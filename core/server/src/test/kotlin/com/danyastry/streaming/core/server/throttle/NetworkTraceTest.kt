package com.danyastry.streaming.core.server.throttle

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class NetworkTraceTest {

    private val trace = NetworkTrace("test", listOf(TraceStep(1_000, 3_000), TraceStep(500, 400)))

    @Test
    fun `looks up the rate for each moment and loops`() {
        assertThat(trace.kilobitsPerSecondAt(0)).isEqualTo(3_000)
        assertThat(trace.kilobitsPerSecondAt(999)).isEqualTo(3_000)
        assertThat(trace.kilobitsPerSecondAt(1_000)).isEqualTo(400)
        assertThat(trace.kilobitsPerSecondAt(1_499)).isEqualTo(400)
        assertThat(trace.kilobitsPerSecondAt(1_500)).isEqualTo(3_000)
        assertThat(trace.kilobitsPerSecondAt(2_600)).isEqualTo(400)
    }

    @Test
    fun `an empty trace is unlimited`() {
        assertThat(NetworkTraces.UNLIMITED.isUnlimited).isTrue()
        assertThat(NetworkTraces.UNLIMITED.kilobitsPerSecondAt(5_000)).isNull()
    }

    @Test
    fun `steps must be real`() {
        assertThrows(IllegalArgumentException::class.java) { TraceStep(0, 100) }
        assertThrows(IllegalArgumentException::class.java) { TraceStep(100, 0) }
        assertThrows(IllegalArgumentException::class.java) { NetworkTrace("x", emptyList(), latencyMillis = -1) }
    }

    @Test
    fun `the presets have distinct names`() {
        assertThat(NetworkTraces.ALL.map { it.name }).containsNoDuplicates()
    }
}
