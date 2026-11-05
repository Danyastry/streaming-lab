package com.danyastry.streaming.core.server.throttle

object NetworkTraces {
    val UNLIMITED = NetworkTrace("Unlimited", emptyList())
    val FAST = NetworkTrace("Steady 4 Mbit/s", listOf(TraceStep(60_000, 4_000)), latencyMillis = 20)
    val SLOW = NetworkTrace("Steady 600 kbit/s", listOf(TraceStep(60_000, 600)), latencyMillis = 80)
    val DROP = NetworkTrace(
        "Drop from 3 Mbit/s to 400 kbit/s and back",
        listOf(TraceStep(8_000, 3_000), TraceStep(8_000, 400), TraceStep(8_000, 3_000)),
        latencyMillis = 40,
    )
    val OSCILLATING = NetworkTrace(
        "Swings between 2.5 Mbit/s and 600 kbit/s",
        listOf(TraceStep(4_000, 2_500), TraceStep(4_000, 600)),
        latencyMillis = 40,
    )

    val ALL: List<NetworkTrace> = listOf(UNLIMITED, FAST, SLOW, DROP, OSCILLATING)
}
