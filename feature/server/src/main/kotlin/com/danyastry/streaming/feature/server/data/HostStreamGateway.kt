package com.danyastry.streaming.feature.server.data

import android.content.Context
import com.danyastry.streaming.core.server.log.RequestRecord
import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.streams.host.StreamHost
import com.danyastry.streaming.feature.server.model.FetchResult
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class HostStreamGateway(context: Context) : StreamGateway {

    private val server = StreamHost.start(context)

    override val baseUrl: String
        get() = server.baseUrl

    override val requests: StateFlow<List<RequestRecord>>
        get() = StreamHost.log.records

    override val trace: NetworkTrace
        get() = server.shaper.trace

    override fun switchTo(trace: NetworkTrace) = server.shaper.switchTo(trace)

    override fun clearLog() = StreamHost.log.clear()

    override suspend fun fetch(path: String): FetchResult = withContext(Dispatchers.IO) {
        val started = System.nanoTime()
        val connection = URL(baseUrl + path).openConnection() as HttpURLConnection
        try {
            val status = connection.responseCode
            val body = (if (status < 400) connection.inputStream else connection.errorStream)?.use { it.readBytes() } ?: ByteArray(0)
            FetchResult(path, status, connection.contentType, body.size, (System.nanoTime() - started) / NANOS_PER_MILLI, body.decodeToString())
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
