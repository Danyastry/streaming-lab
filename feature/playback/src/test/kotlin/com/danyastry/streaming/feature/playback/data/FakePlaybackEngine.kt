package com.danyastry.streaming.feature.playback.data

import androidx.media3.common.Player
import com.danyastry.streaming.core.player.data.PlaybackEngine
import com.danyastry.streaming.core.player.model.PlaybackStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlaybackEngine : PlaybackEngine {

    val played = mutableListOf<Pair<String, String>>()
    var pauses = 0
    var released = false

    override val player: StateFlow<Player?> = MutableStateFlow(null)

    override val stats: StateFlow<PlaybackStats> = MutableStateFlow(PlaybackStats())

    override fun play(url: String, mimeType: String) {
        played += url to mimeType
    }

    override fun pause() {
        pauses++
    }

    override fun release() {
        released = true
    }
}
