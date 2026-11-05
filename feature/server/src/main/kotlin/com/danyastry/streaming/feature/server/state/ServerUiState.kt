package com.danyastry.streaming.feature.server.state

import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.feature.server.model.FetchResult

data class ServerUiState(
    val baseUrl: String,
    val trace: NetworkTrace,
    val fetching: String? = null,
    val fetched: FetchResult? = null,
    val error: String? = null,
)
