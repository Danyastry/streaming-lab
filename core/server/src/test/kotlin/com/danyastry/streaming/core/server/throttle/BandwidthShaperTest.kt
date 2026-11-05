package com.danyastry.streaming.core.server.throttle

import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayOutputStream
import org.junit.Test

class BandwidthShaperTest {

    private class FakeTicker : Ticker {
        var now = 0L
        var slept = 0L

        override fun nanoTime() = now

        override fun sleepNanos(nanos: Long) {
            if (nanos > 0) {
                now += nanos
                slept += nanos
            }
        }
    }

    @Test
    fun `sending takes as long as the trace rate says`() {
        val ticker = FakeTicker()
        val shaper = BandwidthShaper(NetworkTrace("1 Mbit/s", listOf(TraceStep(60_000, 1_000))), ticker, chunkBytes = 1_000)
        val output = ByteArrayOutputStream()

        shaper.write(output, ByteArray(125_000))

        assertThat(output.size()).isEqualTo(125_000)
        assertThat(ticker.slept).isEqualTo(1_000_000_000L)
    }

    @Test
    fun `unlimited sends without waiting`() {
        val ticker = FakeTicker()

        BandwidthShaper(NetworkTraces.UNLIMITED, ticker).write(ByteArrayOutputStream(), ByteArray(1_000_000))

        assertThat(ticker.slept).isEqualTo(0)
    }

    @Test
    fun `the rate changes when the trace moves to the next step`() {
        val ticker = FakeTicker()
        val trace = NetworkTrace("drop", listOf(TraceStep(1_000, 8_000), TraceStep(10_000, 800)))
        val shaper = BandwidthShaper(trace, ticker, chunkBytes = 1_000)

        shaper.write(ByteArrayOutputStream(), ByteArray(1_000_000))
        val fastPart = ticker.now
        shaper.write(ByteArrayOutputStream(), ByteArray(10_000))

        assertThat(fastPart).isEqualTo(1_000_000_000L)
        assertThat(ticker.now - fastPart).isEqualTo(100_000_000L)
    }

    @Test
    fun `two downloads share one link`() {
        val ticker = FakeTicker()
        val shaper = BandwidthShaper(NetworkTrace("1 Mbit/s", listOf(TraceStep(60_000, 1_000))), ticker, chunkBytes = 1_000)

        shaper.write(ByteArrayOutputStream(), ByteArray(1_000))
        shaper.write(ByteArrayOutputStream(), ByteArray(1_000))

        assertThat(ticker.now).isEqualTo(16_000_000L)
    }

    @Test
    fun `latency is waited once per request`() {
        val ticker = FakeTicker()
        val shaper = BandwidthShaper(NetworkTrace("slow", listOf(TraceStep(1_000, 100)), latencyMillis = 80), ticker)

        shaper.awaitLatency()

        assertThat(ticker.slept).isEqualTo(80_000_000L)
    }

    @Test
    fun `switching traces restarts the timeline`() {
        val ticker = FakeTicker()
        val shaper = BandwidthShaper(ticker = ticker)
        ticker.now = 5_000_000_000L

        shaper.switchTo(NetworkTraces.DROP)

        assertThat(shaper.trace).isSameInstanceAs(NetworkTraces.DROP)
        assertThat(shaper.elapsedMillis()).isEqualTo(0)
    }
}
