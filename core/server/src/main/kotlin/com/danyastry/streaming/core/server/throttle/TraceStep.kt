package com.danyastry.streaming.core.server.throttle

data class TraceStep(val durationMillis: Long, val kilobitsPerSecond: Int) {
    init {
        require(durationMillis > 0) { "A trace step must last longer than 0 ms" }
        require(kilobitsPerSecond > 0) { "A trace step needs a positive rate, got $kilobitsPerSecond" }
    }
}
