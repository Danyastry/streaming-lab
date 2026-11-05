package com.danyastry.streaming.core.server.throttle

class NetworkTrace(
    val name: String,
    val steps: List<TraceStep>,
    val latencyMillis: Long = 0,
) {
    val isUnlimited: Boolean
        get() = steps.isEmpty()

    val periodMillis: Long = steps.sumOf { it.durationMillis }

    init {
        require(latencyMillis >= 0) { "Latency cannot be negative" }
    }

    fun kilobitsPerSecondAt(elapsedMillis: Long): Int? {
        if (isUnlimited) return null
        var position = elapsedMillis.coerceAtLeast(0) % periodMillis
        for (step in steps) {
            if (position < step.durationMillis) return step.kilobitsPerSecond
            position -= step.durationMillis
        }
        return steps.last().kilobitsPerSecond
    }
}
