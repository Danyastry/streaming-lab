package com.danyastry.streaming.core.streams.host

import android.content.Context
import com.danyastry.streaming.core.server.StreamServer
import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.streams.content.AssetStreamContent
import com.danyastry.streaming.core.streams.log.RequestLog
import com.danyastry.streaming.core.streams.model.LocalStream

object StreamHost {

    val log = RequestLog()

    @Volatile
    private var server: StreamServer? = null

    @Synchronized
    fun start(context: Context): StreamServer =
        server?.takeIf { it.isRunning }
            ?: StreamServer(AssetStreamContent(context.applicationContext.assets), onRequest = log::add).start().also { server = it }

    fun url(context: Context, stream: LocalStream): String = start(context).baseUrl + stream.path

    fun switchTo(context: Context, trace: NetworkTrace) {
        start(context).shaper.switchTo(trace)
    }
}
