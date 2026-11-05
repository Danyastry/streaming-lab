package com.danyastry.streaming.core.player.logic

import com.danyastry.streaming.core.player.model.PlaybackPhase
import com.danyastry.streaming.core.player.model.PlaybackStats
import com.danyastry.streaming.core.player.model.PlayerEvent
import com.danyastry.streaming.core.player.model.RenditionSwitch

object PlaybackStatsReducer {

    const val MAX_SWITCHES = 50

    fun reduce(stats: PlaybackStats, event: PlayerEvent, nowMillis: Long): PlaybackStats = when (event) {
        PlayerEvent.Started -> PlaybackStats(startedAtMillis = nowMillis)
        is PlayerEvent.PhaseChanged -> onPhase(stats, event.phase, nowMillis)
        is PlayerEvent.PlayingChanged -> stats.copy(isPlaying = event.isPlaying)
        PlayerEvent.SeekStarted -> stats.copy(seeking = true)
        is PlayerEvent.Progress -> stats.copy(
            positionMillis = event.positionMillis,
            bufferedMillis = (event.bufferedPositionMillis - event.positionMillis).coerceAtLeast(0),
        )
        is PlayerEvent.LoadingRenditionChanged ->
            if (event.rendition == stats.loading) {
                stats
            } else {
                val switch = RenditionSwitch(event.positionMillis, event.mediaMillis, stats.loading, event.rendition, event.reason)
                stats.copy(loading = event.rendition, switches = (stats.switches + switch).takeLast(MAX_SWITCHES))
            }
        is PlayerEvent.PlayingRenditionChanged -> stats.copy(playing = event.rendition)
        is PlayerEvent.SegmentLoaded -> stats.copy(segments = stats.segments + 1, loadedBytes = stats.loadedBytes + event.bytes)
        is PlayerEvent.BandwidthEstimated -> stats.copy(bandwidthEstimate = event.bitsPerSecond)
        is PlayerEvent.FramesDropped -> stats.copy(droppedFrames = stats.droppedFrames + event.count)
        is PlayerEvent.Failed -> stats.copy(error = event.message)
    }

    private fun onPhase(stats: PlaybackStats, phase: PlaybackPhase, nowMillis: Long): PlaybackStats {
        val started = stats.startupMillis != null
        return when {
            phase == PlaybackPhase.READY && !started -> stats.copy(
                phase = phase,
                startupMillis = stats.startedAtMillis?.let { nowMillis - it } ?: 0,
                seeking = false,
            )
            phase == PlaybackPhase.READY -> stats.copy(
                phase = phase,
                stalledMillis = stats.stalledMillis + (stats.stallStartedAtMillis?.let { nowMillis - it } ?: 0),
                stallStartedAtMillis = null,
                seeking = false,
            )
            phase == PlaybackPhase.BUFFERING && started && !stats.seeking && stats.stallStartedAtMillis == null -> stats.copy(
                phase = phase,
                stalls = stats.stalls + 1,
                stallStartedAtMillis = nowMillis,
            )
            else -> stats.copy(phase = phase)
        }
    }
}
