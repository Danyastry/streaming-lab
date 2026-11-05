package com.danyastry.streaming.core.server.http

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ByteRangeTest {

    @Test
    fun `no header or another unit means the whole file`() {
        assertThat(ByteRange.parse(null, 100)).isEqualTo(RangeResult.Full)
        assertThat(ByteRange.parse("items=0-1", 100)).isEqualTo(RangeResult.Full)
    }

    @Test
    fun `closed, open and suffix ranges`() {
        assertThat(ByteRange.parse("bytes=0-9", 100)).isEqualTo(RangeResult.Partial(ByteRange(0, 9)))
        assertThat(ByteRange.parse("bytes=90-", 100)).isEqualTo(RangeResult.Partial(ByteRange(90, 99)))
        assertThat(ByteRange.parse("bytes=-10", 100)).isEqualTo(RangeResult.Partial(ByteRange(90, 99)))
        assertThat(ByteRange.parse("bytes=-500", 100)).isEqualTo(RangeResult.Partial(ByteRange(0, 99)))
    }

    @Test
    fun `an end past the file is clipped`() {
        assertThat(ByteRange.parse("bytes=50-1000", 100)).isEqualTo(RangeResult.Partial(ByteRange(50, 99)))
        assertThat(ByteRange(50, 99).length).isEqualTo(50)
    }

    @Test
    fun `ranges that cannot be served are unsatisfiable`() {
        listOf("bytes=100-", "bytes=200-300", "bytes=9-5", "bytes=x-5", "bytes=-0", "bytes=5").forEach {
            assertThat(ByteRange.parse(it, 100)).isEqualTo(RangeResult.Unsatisfiable)
        }
    }

    @Test
    fun `multipart ranges fall back to the whole file`() {
        assertThat(ByteRange.parse("bytes=0-1,5-6", 100)).isEqualTo(RangeResult.Full)
    }
}
