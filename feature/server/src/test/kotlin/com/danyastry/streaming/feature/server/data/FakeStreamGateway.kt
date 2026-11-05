package com.danyastry.streaming.feature.server.data

import com.danyastry.streaming.core.server.log.RequestRecord
import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.server.throttle.NetworkTraces
import com.danyastry.streaming.feature.server.model.FetchResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeStreamGateway : StreamGateway {

    val log = MutableStateFlow<List<RequestRecord>>(emptyList())

    var response = CompletableDeferred<FetchResult>()

    val switched = mutableListOf<NetworkTrace>()

    override val baseUrl = "http://127.0.0.1:4321/"

    override val requests: StateFlow<List<RequestRecord>> = log

    override var trace: NetworkTrace = NetworkTraces.UNLIMITED
        private set

    override fun switchTo(trace: NetworkTrace) {
        this.trace = trace
        switched += trace
    }

    override fun clearLog() {
        log.value = emptyList()
    }

    override suspend fun fetch(path: String): FetchResult = response.await()
}
