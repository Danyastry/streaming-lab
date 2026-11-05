package com.danyastry.streaming.feature.server.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danyastry.streaming.core.server.log.RequestRecord
import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.feature.server.data.StreamGateway
import com.danyastry.streaming.feature.server.state.ServerUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServerViewModel(private val gateway: StreamGateway) : ViewModel() {

    private val _state = MutableStateFlow(ServerUiState(gateway.baseUrl, gateway.trace))
    val state: StateFlow<ServerUiState> = _state.asStateFlow()

    val requests: StateFlow<List<RequestRecord>> = gateway.requests

    fun onTraceChange(trace: NetworkTrace) {
        gateway.switchTo(trace)
        _state.update { it.copy(trace = trace) }
    }

    fun onFetch(path: String) {
        if (_state.value.fetching != null) return
        _state.update { it.copy(fetching = path, error = null) }
        viewModelScope.launch {
            try {
                val result = gateway.fetch(path)
                _state.update { it.copy(fetched = result) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _state.update { it.copy(error = error.message ?: error.javaClass.simpleName) }
            } finally {
                _state.update { it.copy(fetching = null) }
            }
        }
    }

    fun onClearLog() = gateway.clearLog()
}
