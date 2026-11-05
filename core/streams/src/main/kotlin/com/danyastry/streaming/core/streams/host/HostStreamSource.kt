package com.danyastry.streaming.core.streams.host

import android.content.Context
import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.streams.model.LocalStream

class HostStreamSource(context: Context) : StreamSource {

    private val context = context.applicationContext

    override val trace: NetworkTrace
        get() = StreamHost.start(context).shaper.trace

    override fun url(stream: LocalStream): String = StreamHost.url(context, stream)

    override fun switchTo(trace: NetworkTrace) = StreamHost.switchTo(context, trace)
}
