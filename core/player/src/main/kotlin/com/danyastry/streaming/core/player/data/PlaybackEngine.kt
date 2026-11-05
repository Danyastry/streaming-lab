package com.danyastry.streaming.core.player.data

import androidx.media3.common.Player
import com.danyastry.streaming.core.player.model.PlaybackStats
import kotlinx.coroutines.flow.StateFlow

interface PlaybackEngine {
    val player: StateFlow<Player?>

    val stats: StateFlow<PlaybackStats>

    fun play(url: String, mimeType: String)

    fun pause()

    fun release()
}
