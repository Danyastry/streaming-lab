package com.danyastry.streaming.core.server.throttle

import java.io.OutputStream

class BandwidthShaper(
    trace: NetworkTrace = NetworkTraces.UNLIMITED,
    private val ticker: Ticker = Ticker.SYSTEM,
    private val chunkBytes: Int = DEFAULT_CHUNK_BYTES,
) {
    @Volatile
    var trace: NetworkTrace = trace
        private set

    private var startNanos = ticker.nanoTime()
    private var linkFreeAtNanos = startNanos

    @Synchronized
    fun switchTo(trace: NetworkTrace) {
        this.trace = trace
        startNanos = ticker.nanoTime()
        linkFreeAtNanos = startNanos
    }

    fun elapsedMillis(): Long = (ticker.nanoTime() - startNanos) / NANOS_PER_MILLI

    fun awaitLatency() {
        ticker.sleepNanos(trace.latencyMillis * NANOS_PER_MILLI)
    }

    fun write(output: OutputStream, bytes: ByteArray, offset: Int = 0, length: Int = bytes.size) {
        var position = offset
        val end = offset + length
        while (position < end) {
            val size = minOf(chunkBytes, end - position)
            reserve(size)?.let { readyAt -> ticker.sleepNanos(readyAt - ticker.nanoTime()) }
            output.write(bytes, position, size)
            position += size
        }
        output.flush()
    }

    @Synchronized
    private fun reserve(bytes: Int): Long? {
        val rate = trace.kilobitsPerSecondAt(elapsedMillis()) ?: return null
        val sendNanos = bytes * BITS_PER_BYTE * NANOS_PER_SECOND / (rate * BITS_PER_KILOBIT)
        val start = maxOf(ticker.nanoTime(), linkFreeAtNanos)
        linkFreeAtNanos = start + sendNanos
        return linkFreeAtNanos
    }

    companion object {
        const val DEFAULT_CHUNK_BYTES = 4 * 1024
        private const val BITS_PER_BYTE = 8L
        private const val BITS_PER_KILOBIT = 1_000L
        private const val NANOS_PER_SECOND = 1_000_000_000L
        private const val NANOS_PER_MILLI = 1_000_000L
    }
}
