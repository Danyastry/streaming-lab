package com.danyastry.streaming.core.player.logic

import com.danyastry.streaming.core.player.model.PlaybackPhase
import com.danyastry.streaming.core.player.model.PlaybackStats
import com.danyastry.streaming.core.player.model.PlayerEvent
import com.danyastry.streaming.core.player.model.RenditionSwitch
import com.danyastry.streaming.core.player.model.SwitchReason
import com.danyastry.streaming.core.player.model.VideoRendition
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaybackStatsReducerTest {

    private val low = VideoRendition(640, 360, 568_550)
    private val high = VideoRendition(1280, 720, 2_068_550)

    private fun run(vararg steps: Pair<Long, PlayerEvent>): PlaybackStats =
        steps.fold(PlaybackStats()) { stats, (now, event) -> PlaybackStatsReducer.reduce(stats, event, now) }

    @Test
    fun `startup runs from the play call to the first ready state`() {
        val stats = run(
            1_000L to PlayerEvent.Started,
            1_010L to PlayerEvent.PhaseChanged(PlaybackPhase.BUFFERING),
            1_450L to PlayerEvent.PhaseChanged(PlaybackPhase.READY),
        )

        assertThat(stats.startupMillis).isEqualTo(450)
        assertThat(stats.stalls).isEqualTo(0)
        assertThat(stats.phase).isEqualTo(PlaybackPhase.READY)
    }

    @Test
    fun `buffering after the first ready state is a stall with its duration`() {
        val stats = run(
            0L to PlayerEvent.Started,
            100L to PlayerEvent.PhaseChanged(PlaybackPhase.READY),
            5_000L to PlayerEvent.PhaseChanged(PlaybackPhase.BUFFERING),
            5_800L to PlayerEvent.PhaseChanged(PlaybackPhase.READY),
            9_000L to PlayerEvent.PhaseChanged(PlaybackPhase.BUFFERING),
            9_200L to PlayerEvent.PhaseChanged(PlaybackPhase.READY),
        )

        assertThat(stats.stalls).isEqualTo(2)
        assertThat(stats.stalledMillis).isEqualTo(1_000)
    }

    @Test
    fun `buffering caused by a seek is not a stall`() {
        val stats = run(
            0L to PlayerEvent.Started,
            100L to PlayerEvent.PhaseChanged(PlaybackPhase.READY),
            2_000L to PlayerEvent.SeekStarted,
            2_001L to PlayerEvent.PhaseChanged(PlaybackPhase.BUFFERING),
            2_300L to PlayerEvent.PhaseChanged(PlaybackPhase.READY),
            4_000L to PlayerEvent.PhaseChanged(PlaybackPhase.BUFFERING),
            4_100L to PlayerEvent.PhaseChanged(PlaybackPhase.READY),
        )

        assertThat(stats.stalls).isEqualTo(1)
        assertThat(stats.stalledMillis).isEqualTo(100)
    }

    @Test
    fun `each new loading rendition is logged with its reason`() {
        val stats = run(
            0L to PlayerEvent.Started,
            10L to PlayerEvent.LoadingRenditionChanged(low, SwitchReason.INITIAL, 0, 0),
            20L to PlayerEvent.LoadingRenditionChanged(low, SwitchReason.ADAPTIVE, 500, 2_000),
            30L to PlayerEvent.LoadingRenditionChanged(high, SwitchReason.ADAPTIVE, 900, 10_000),
        )

        assertThat(stats.loading).isEqualTo(high)
        assertThat(stats.switches).containsExactly(
            RenditionSwitch(0, 0, null, low, SwitchReason.INITIAL),
            RenditionSwitch(900, 10_000, low, high, SwitchReason.ADAPTIVE),
        ).inOrder()
    }

    @Test
    fun `the switch log keeps only the newest entries`() {
        var stats = PlaybackStats()
        repeat(PlaybackStatsReducer.MAX_SWITCHES + 5) { index ->
            val rendition = if (index % 2 == 0) low else high
            stats = PlaybackStatsReducer.reduce(stats, PlayerEvent.LoadingRenditionChanged(rendition, SwitchReason.ADAPTIVE, index.toLong(), index * 2_000L), 0)
        }

        assertThat(stats.switches).hasSize(PlaybackStatsReducer.MAX_SWITCHES)
        assertThat(stats.switches.last().positionMillis).isEqualTo(PlaybackStatsReducer.MAX_SWITCHES + 4L)
    }

    @Test
    fun `progress keeps the buffer ahead of the position`() {
        val stats = run(
            0L to PlayerEvent.Progress(positionMillis = 3_000, bufferedPositionMillis = 11_000),
        )

        assertThat(stats.positionMillis).isEqualTo(3_000)
        assertThat(stats.bufferedMillis).isEqualTo(8_000)
        assertThat(run(0L to PlayerEvent.Progress(5_000, 4_000)).bufferedMillis).isEqualTo(0)
    }

    @Test
    fun `segments, bytes and dropped frames add up`() {
        val stats = run(
            0L to PlayerEvent.SegmentLoaded(1_000),
            0L to PlayerEvent.SegmentLoaded(2_500),
            0L to PlayerEvent.FramesDropped(3),
            0L to PlayerEvent.FramesDropped(2),
            0L to PlayerEvent.BandwidthEstimated(1_800_000),
        )

        assertThat(stats.segments).isEqualTo(2)
        assertThat(stats.loadedBytes).isEqualTo(3_500)
        assertThat(stats.droppedFrames).isEqualTo(5)
        assertThat(stats.bandwidthEstimate).isEqualTo(1_800_000)
    }

    @Test
    fun `starting again resets everything`() {
        val stats = run(
            0L to PlayerEvent.Started,
            100L to PlayerEvent.PhaseChanged(PlaybackPhase.READY),
            200L to PlayerEvent.SegmentLoaded(1_000),
            300L to PlayerEvent.Failed("ERROR_CODE_IO_NETWORK_CONNECTION_FAILED: refused"),
            1_000L to PlayerEvent.Started,
        )

        assertThat(stats).isEqualTo(PlaybackStats(startedAtMillis = 1_000))
    }
}
