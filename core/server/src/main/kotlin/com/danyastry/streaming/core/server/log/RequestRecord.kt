package com.danyastry.streaming.core.server.log

data class RequestRecord(
    val path: String,
    val status: Int,
    val range: String?,
    val bytes: Long,
    val startedAtMillis: Long,
    val durationMillis: Long,
    val traceName: String,
) {
    val kilobitsPerSecond: Long?
        get() = if (durationMillis > 0) bytes * BITS_PER_BYTE / durationMillis else null

    private companion object {
        const val BITS_PER_BYTE = 8L
    }
}
