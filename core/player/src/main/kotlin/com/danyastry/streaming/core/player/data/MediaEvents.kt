package com.danyastry.streaming.core.player.data

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.danyastry.streaming.core.player.model.PlaybackPhase
import com.danyastry.streaming.core.player.model.SwitchReason
import com.danyastry.streaming.core.player.model.VideoRendition

@OptIn(UnstableApi::class)
internal object MediaEvents {

    fun phase(state: Int): PlaybackPhase = when (state) {
        Player.STATE_BUFFERING -> PlaybackPhase.BUFFERING
        Player.STATE_READY -> PlaybackPhase.READY
        Player.STATE_ENDED -> PlaybackPhase.ENDED
        else -> PlaybackPhase.IDLE
    }

    fun reason(reason: Int): SwitchReason = when (reason) {
        C.SELECTION_REASON_INITIAL -> SwitchReason.INITIAL
        C.SELECTION_REASON_ADAPTIVE -> SwitchReason.ADAPTIVE
        C.SELECTION_REASON_MANUAL -> SwitchReason.MANUAL
        C.SELECTION_REASON_TRICK_PLAY -> SwitchReason.TRICK_PLAY
        else -> SwitchReason.UNKNOWN
    }

    fun isVideo(trackType: Int, format: Format): Boolean =
        trackType == C.TRACK_TYPE_VIDEO || (trackType == C.TRACK_TYPE_DEFAULT && format.height > 0)

    fun rendition(format: Format): VideoRendition = VideoRendition(
        width = format.width.coerceAtLeast(0),
        height = format.height.coerceAtLeast(0),
        bitsPerSecond = format.bitrate.takeIf { it != Format.NO_VALUE }?.toLong() ?: 0,
    )
}
