package com.danyastry.streaming.feature.playback.data

import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.server.throttle.NetworkTraces
import com.danyastry.streaming.core.streams.host.StreamSource
import com.danyastry.streaming.core.streams.model.LocalStream

class FakeStreamSource : StreamSource {

    val switched = mutableListOf<NetworkTrace>()

    override var trace: NetworkTrace = NetworkTraces.FAST
        private set

    override fun url(stream: LocalStream): String = "http://127.0.0.1:4321/" + stream.path

    override fun switchTo(trace: NetworkTrace) {
        this.trace = trace
        switched += trace
    }
}
