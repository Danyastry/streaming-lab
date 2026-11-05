package com.danyastry.streaming.core.player.model

data class PlaybackStats(
    val phase: PlaybackPhase = PlaybackPhase.IDLE,
    val isPlaying: Boolean = false,
    val positionMillis: Long = 0,
    val bufferedMillis: Long = 0,
    val loading: VideoRendition? = null,
    val playing: VideoRendition? = null,
    val bandwidthEstimate: Long? = null,
    val droppedFrames: Int = 0,
    val startupMillis: Long? = null,
    val stalls: Int = 0,
    val stalledMillis: Long = 0,
    val segments: Int = 0,
    val loadedBytes: Long = 0,
    val switches: List<RenditionSwitch> = emptyList(),
    val error: String? = null,
    val startedAtMillis: Long? = null,
    val stallStartedAtMillis: Long? = null,
    val seeking: Boolean = false,
)
