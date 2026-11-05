package com.danyastry.streaming.feature.playback.state

import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.streams.model.LocalStream

data class PlaybackUiState(
    val stream: LocalStream,
    val trace: NetworkTrace,
    val url: String? = null,
)
