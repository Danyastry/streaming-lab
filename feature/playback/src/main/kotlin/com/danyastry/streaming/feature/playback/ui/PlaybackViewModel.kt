package com.danyastry.streaming.feature.playback.ui

import androidx.lifecycle.ViewModel
import androidx.media3.common.Player
import com.danyastry.streaming.core.player.data.PlaybackEngine
import com.danyastry.streaming.core.player.model.PlaybackStats
import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.streams.host.StreamSource
import com.danyastry.streaming.core.streams.model.LocalStream
import com.danyastry.streaming.core.streams.model.LocalStreams
import com.danyastry.streaming.feature.playback.logic.StreamMimeTypes
import com.danyastry.streaming.feature.playback.state.PlaybackUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlaybackViewModel(
    private val engine: PlaybackEngine,
    private val source: StreamSource,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaybackUiState(LocalStreams.VOD_HLS, source.trace))
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    val stats: StateFlow<PlaybackStats> = engine.stats

    val player: StateFlow<Player?> = engine.player

    init {
        play()
    }

    fun onStreamChange(stream: LocalStream) {
        _state.update { it.copy(stream = stream) }
        play()
    }

    fun onTraceChange(trace: NetworkTrace) {
        source.switchTo(trace)
        _state.update { it.copy(trace = trace) }
    }

    fun onRestart() = play()

    fun onStop() = engine.pause()

    private fun play() {
        val current = _state.value
        source.switchTo(current.trace)
        val url = source.url(current.stream)
        _state.update { it.copy(url = url) }
        engine.play(url, StreamMimeTypes.of(current.stream.format))
    }

    override fun onCleared() = engine.release()
}
