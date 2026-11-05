package com.danyastry.streaming.core.server

import com.danyastry.streaming.core.server.content.StreamContent
import com.danyastry.streaming.core.server.log.RequestRecord
import com.danyastry.streaming.core.server.throttle.BandwidthShaper
import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.server.throttle.TraceStep
import com.google.common.truth.Truth.assertThat
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.After
import org.junit.Test

class StreamServerTest {

    private val files = mapOf(
        "vod/master.m3u8" to "#EXTM3U\n".toByteArray(),
        "vod/chunk-0-00001.m4s" to ByteArray(1_000) { it.toByte() },
    )
    private val log = CopyOnWriteArrayList<RequestRecord>()
    private val server = StreamServer(StreamContent { files[it] }, onRequest = { log += it }).start()

    @After
    fun tearDown() = server.close()

    @Test
    fun `serves a whole file with its type and length`() {
        val connection = open("vod/master.m3u8")

        assertThat(connection.responseCode).isEqualTo(200)
        assertThat(connection.contentType).isEqualTo("application/vnd.apple.mpegurl")
        assertThat(connection.getHeaderField("Accept-Ranges")).isEqualTo("bytes")
        assertThat(connection.inputStream.readBytes().decodeToString()).isEqualTo("#EXTM3U\n")
    }

    @Test
    fun `serves byte ranges with 206 and content range`() {
        val connection = open("vod/chunk-0-00001.m4s", range = "bytes=10-19")

        assertThat(connection.responseCode).isEqualTo(206)
        assertThat(connection.getHeaderField("Content-Range")).isEqualTo("bytes 10-19/1000")
        assertThat(connection.contentType).isEqualTo("video/iso.segment")
        assertThat(connection.inputStream.readBytes().toList()).isEqualTo((10..19).map { it.toByte() })
    }

    @Test
    fun `head answers without a body`() {
        val connection = open("vod/chunk-0-00001.m4s", method = "HEAD")

        assertThat(connection.responseCode).isEqualTo(200)
        assertThat(connection.contentLengthLong).isEqualTo(1_000)
    }

    @Test
    fun `missing files, escapes and bad ranges are refused`() {
        assertThat(open("vod/missing.m4s").responseCode).isEqualTo(404)
        assertThat(open("vod/../secret").responseCode).isEqualTo(404)
        val unsatisfiable = open("vod/chunk-0-00001.m4s", range = "bytes=5000-")
        assertThat(unsatisfiable.responseCode).isEqualTo(416)
        assertThat(unsatisfiable.getHeaderField("Content-Range")).isEqualTo("bytes */1000")
    }

    @Test
    fun `every request is logged with its status and size`() {
        open("vod/master.m3u8").inputStream.readBytes()
        open("vod/chunk-0-00001.m4s", range = "bytes=0-99").inputStream.readBytes()
        open("vod/missing.m4s").responseCode

        waitFor { log.size == 3 }
        assertThat(log.map { Triple(it.path, it.status, it.bytes) }).containsExactly(
            Triple("vod/master.m3u8", 200, 8L),
            Triple("vod/chunk-0-00001.m4s", 206, 100L),
            Triple("vod/missing.m4s", 404, 0L),
        ).inOrder()
    }

    @Test
    fun `a throttled download really takes as long as the trace says`() {
        val payload = ByteArray(100_000)
        val shaper = BandwidthShaper(NetworkTrace("1.6 Mbit/s", listOf(TraceStep(60_000, 1_600))))
        StreamServer(StreamContent { payload }, shaper).start().use { slow ->
            val started = System.nanoTime()
            val body = (URL(slow.baseUrl + "big.m4s").openConnection() as HttpURLConnection).inputStream.readBytes()
            val millis = (System.nanoTime() - started) / 1_000_000

            assertThat(body.size).isEqualTo(100_000)
            assertThat(millis).isAtLeast(450L)
            assertThat(millis).isLessThan(1_500L)
        }
    }

    private fun open(path: String, range: String? = null, method: String = "GET"): HttpURLConnection =
        (URL(server.baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            range?.let { setRequestProperty("Range", it) }
        }

    private fun waitFor(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 2_000
        while (!condition() && System.currentTimeMillis() < deadline) Thread.sleep(10)
    }
}
