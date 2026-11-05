package com.danyastry.streaming.feature.server.model

data class FetchResult(
    val path: String,
    val status: Int,
    val contentType: String?,
    val bytes: Int,
    val millis: Long,
    val text: String,
)
