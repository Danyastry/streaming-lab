package com.danyastry.streaming.core.server.http

sealed interface RangeResult {
    data object Full : RangeResult

    data class Partial(val range: ByteRange) : RangeResult

    data object Unsatisfiable : RangeResult
}
