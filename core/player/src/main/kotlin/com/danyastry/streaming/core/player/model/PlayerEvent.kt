package com.danyastry.streaming.core.player.model

sealed interface PlayerEvent {

    data object Started : PlayerEvent

    data class PhaseChanged(val phase: PlaybackPhase) : PlayerEvent

    data class PlayingChanged(val isPlaying: Boolean) : PlayerEvent

    data object SeekStarted : PlayerEvent

    data class Progress(val positionMillis: Long, val bufferedPositionMillis: Long) : PlayerEvent

    data class LoadingRenditionChanged(
        val rendition: VideoRendition,
        val reason: SwitchReason,
        val positionMillis: Long,
        val mediaMillis: Long,
    ) : PlayerEvent

    data class PlayingRenditionChanged(val rendition: VideoRendition) : PlayerEvent

    data class SegmentLoaded(val bytes: Long) : PlayerEvent

    data class BandwidthEstimated(val bitsPerSecond: Long) : PlayerEvent

    data class FramesDropped(val count: Int) : PlayerEvent

    data class Failed(val message: String) : PlayerEvent
}
