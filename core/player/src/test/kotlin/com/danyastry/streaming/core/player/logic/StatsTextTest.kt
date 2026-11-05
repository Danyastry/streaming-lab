package com.danyastry.streaming.core.player.logic

import com.danyastry.streaming.core.player.model.VideoRendition
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StatsTextTest {

    @Test
    fun `bitrates use kbit below one megabit and Mbit above`() {
        assertThat(StatsText.bitrate(568_550)).isEqualTo("568 kbit/s")
        assertThat(StatsText.bitrate(2_068_550)).isEqualTo("2.07 Mbit/s")
        assertThat(StatsText.bitrate(null)).isEqualTo("-")
        assertThat(StatsText.bitrate(0)).isEqualTo("-")
    }

    @Test
    fun `seconds have one decimal`() {
        assertThat(StatsText.seconds(12_345)).isEqualTo("12.3 s")
        assertThat(StatsText.seconds(null)).isEqualTo("-")
    }

    @Test
    fun `bytes scale to KB and MB`() {
        assertThat(StatsText.bytes(900)).isEqualTo("900 B")
        assertThat(StatsText.bytes(4_096)).isEqualTo("4 KB")
        assertThat(StatsText.bytes(3_670_016)).isEqualTo("3.5 MB")
    }

    @Test
    fun `rendition shows height and bitrate`() {
        assertThat(StatsText.rendition(VideoRendition(960, 540, 1_068_550))).isEqualTo("540p - 1.07 Mbit/s")
        assertThat(StatsText.rendition(null)).isEqualTo("-")
    }
}
