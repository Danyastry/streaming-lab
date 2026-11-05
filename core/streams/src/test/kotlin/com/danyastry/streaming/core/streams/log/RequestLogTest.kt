package com.danyastry.streaming.core.streams.log

import com.danyastry.streaming.core.server.log.RequestRecord
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RequestLogTest {

    @Test
    fun `keeps the newest records first up to the limit`() {
        val log = RequestLog(limit = 2)

        (1..3).forEach { log.add(record("chunk-$it.m4s")) }

        assertThat(log.records.value.map { it.path }).containsExactly("chunk-3.m4s", "chunk-2.m4s").inOrder()
        log.clear()
        assertThat(log.records.value).isEmpty()
    }

    @Test
    fun `measured throughput comes from bytes and duration`() {
        assertThat(record("a", bytes = 125_000, durationMillis = 500).kilobitsPerSecond).isEqualTo(2_000)
        assertThat(record("a", durationMillis = 0).kilobitsPerSecond).isNull()
    }

    private fun record(path: String, bytes: Long = 10, durationMillis: Long = 5) =
        RequestRecord(path, 200, null, bytes, 0, durationMillis, "Unlimited")
}
