package com.danyastry.streaming.core.server

import com.danyastry.streaming.core.server.content.MimeTypes
import com.danyastry.streaming.core.server.content.StreamContent
import com.danyastry.streaming.core.server.http.ByteRange
import com.danyastry.streaming.core.server.http.HttpRequest
import com.danyastry.streaming.core.server.http.RangeResult
import com.danyastry.streaming.core.server.log.RequestRecord
import com.danyastry.streaming.core.server.throttle.BandwidthShaper
import java.io.Closeable
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class StreamServer(
    private val content: StreamContent,
    val shaper: BandwidthShaper = BandwidthShaper(),
    private val onRequest: (RequestRecord) -> Unit = {},
) : Closeable {

    private var socket: ServerSocket? = null
    private var workers: ExecutorService? = null

    val port: Int
        get() = checkNotNull(socket) { "The server is not running" }.localPort

    val baseUrl: String
        get() = "http://127.0.0.1:$port/"

    val isRunning: Boolean
        get() = socket?.isClosed == false

    @Synchronized
    fun start(port: Int = 0): StreamServer {
        if (isRunning) return this
        val server = ServerSocket(port, BACKLOG, InetAddress.getByAddress(LOOPBACK_IPV4))
        val pool = Executors.newCachedThreadPool { runnable -> Thread(runnable, "stream-server").apply { isDaemon = true } }
        socket = server
        workers = pool
        pool.execute {
            while (!server.isClosed) {
                val client = try {
                    server.accept()
                } catch (_: IOException) {
                    break
                }
                pool.execute { serve(client) }
            }
        }
        return this
    }

    @Synchronized
    override fun close() {
        socket?.close()
        workers?.shutdownNow()
        socket = null
        workers = null
    }

    private fun serve(client: Socket) {
        client.use(::handle)
    }

    private fun handle(client: Socket) {
        val started = System.currentTimeMillis()
        val request = HttpRequest.read(client.getInputStream()) ?: return
        val output = client.getOutputStream().buffered()
        val path = request.path.removePrefix("/")
        val record = { status: Int, bytes: Long, range: String? ->
            onRequest(RequestRecord(path, status, range, bytes, started, System.currentTimeMillis() - started, shaper.trace.name))
        }
        if (request.method != "GET" && request.method != "HEAD") {
            respond(output, 405, "Method Not Allowed")
            record(405, 0, null)
            return
        }
        val body = if (isSafe(path)) content.read(path) else null
        if (body == null) {
            respond(output, 404, "Not Found")
            record(404, 0, null)
            return
        }
        val rangeHeader = request.header("range")
        when (val range = ByteRange.parse(rangeHeader, body.size.toLong())) {
            RangeResult.Unsatisfiable -> {
                respond(output, 416, "Range Not Satisfiable", listOf("Content-Range" to "bytes */${body.size}"))
                record(416, 0, rangeHeader)
            }
            RangeResult.Full -> send(output, request, path, body, 0, body.size, 200, "OK", emptyList()).also { record(200, it, null) }
            is RangeResult.Partial -> {
                val headers = listOf("Content-Range" to "bytes ${range.range.first}-${range.range.last}/${body.size}")
                send(output, request, path, body, range.range.first.toInt(), range.range.length.toInt(), 206, "Partial Content", headers)
                    .also { record(206, it, rangeHeader) }
            }
        }
    }

    private fun send(
        output: OutputStream,
        request: HttpRequest,
        path: String,
        body: ByteArray,
        offset: Int,
        length: Int,
        status: Int,
        reason: String,
        extra: List<Pair<String, String>>,
    ): Long {
        val headers = listOf("Content-Type" to MimeTypes.of(path), "Accept-Ranges" to "bytes") + extra
        respond(output, status, reason, headers, contentLength = length.toLong())
        if (request.method == "HEAD") return 0
        shaper.awaitLatency()
        shaper.write(output, body, offset, length)
        return length.toLong()
    }

    private fun respond(output: OutputStream, status: Int, reason: String, headers: List<Pair<String, String>> = emptyList(), contentLength: Long = 0) {
        val head = buildString {
            append("HTTP/1.1 $status $reason\r\n")
            headers.forEach { (name, value) -> append("$name: $value\r\n") }
            append("Content-Length: $contentLength\r\n")
            append("Connection: close\r\n\r\n")
        }
        output.write(head.toByteArray(Charsets.US_ASCII))
        output.flush()
    }

    private fun isSafe(path: String): Boolean = path.isNotEmpty() && path.split('/').none { it == ".." || it.isEmpty() }

    private companion object {
        const val BACKLOG = 16
        val LOOPBACK_IPV4 = byteArrayOf(127, 0, 0, 1)
    }
}
