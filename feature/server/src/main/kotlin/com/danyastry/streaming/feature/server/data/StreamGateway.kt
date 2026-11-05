package com.danyastry.streaming.feature.server.data

import com.danyastry.streaming.core.server.log.RequestRecord
import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.feature.server.model.FetchResult
import kotlinx.coroutines.flow.StateFlow

interface StreamGateway {
    val baseUrl: String

    val requests: StateFlow<List<RequestRecord>>

    val trace: NetworkTrace

    fun switchTo(trace: NetworkTrace)

    fun clearLog()

    suspend fun fetch(path: String): FetchResult
}
