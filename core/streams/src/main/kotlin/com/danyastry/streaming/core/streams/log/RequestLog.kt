package com.danyastry.streaming.core.streams.log

import com.danyastry.streaming.core.server.log.RequestRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RequestLog(private val limit: Int = DEFAULT_LIMIT) {

    private val _records = MutableStateFlow<List<RequestRecord>>(emptyList())
    val records: StateFlow<List<RequestRecord>> = _records.asStateFlow()

    fun add(record: RequestRecord) = _records.update { (listOf(record) + it).take(limit) }

    fun clear() {
        _records.value = emptyList()
    }

    private companion object {
        const val DEFAULT_LIMIT = 200
    }
}
