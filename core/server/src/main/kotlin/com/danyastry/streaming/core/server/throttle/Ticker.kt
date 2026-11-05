package com.danyastry.streaming.core.server.throttle

interface Ticker {
    fun nanoTime(): Long

    fun sleepNanos(nanos: Long)

    companion object {
        val SYSTEM: Ticker = object : Ticker {
            override fun nanoTime(): Long = System.nanoTime()

            override fun sleepNanos(nanos: Long) {
                if (nanos > 0) Thread.sleep(nanos / NANOS_PER_MILLI, (nanos % NANOS_PER_MILLI).toInt())
            }
        }

        private const val NANOS_PER_MILLI = 1_000_000L
    }
}
