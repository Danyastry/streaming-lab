package com.danyastry.streaming.core.server.http

data class ByteRange(val first: Long, val last: Long) {
    val length: Long
        get() = last - first + 1

    companion object {
        fun parse(header: String?, totalLength: Long): RangeResult {
            if (header == null) return RangeResult.Full
            val spec = header.trim().removePrefix("bytes=").takeIf { header.trim().startsWith("bytes=") } ?: return RangeResult.Full
            if (',' in spec) return RangeResult.Full
            val dash = spec.indexOf('-').takeIf { it >= 0 } ?: return RangeResult.Unsatisfiable
            val startText = spec.substring(0, dash).trim()
            val endText = spec.substring(dash + 1).trim()
            val range = when {
                startText.isEmpty() -> {
                    val suffix = endText.toLongOrNull()?.takeIf { it > 0 } ?: return RangeResult.Unsatisfiable
                    ByteRange((totalLength - suffix).coerceAtLeast(0), totalLength - 1)
                }
                else -> {
                    val start = startText.toLongOrNull() ?: return RangeResult.Unsatisfiable
                    val end = if (endText.isEmpty()) totalLength - 1 else endText.toLongOrNull() ?: return RangeResult.Unsatisfiable
                    if (end < start) return RangeResult.Unsatisfiable
                    ByteRange(start, minOf(end, totalLength - 1))
                }
            }
            return if (range.first >= totalLength || totalLength == 0L) RangeResult.Unsatisfiable else RangeResult.Partial(range)
        }
    }
}
