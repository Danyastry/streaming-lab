package com.danyastry.streaming.core.streams.host

import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.streams.model.LocalStream

interface StreamSource {
    val trace: NetworkTrace

    fun url(stream: LocalStream): String

    fun switchTo(trace: NetworkTrace)
}
