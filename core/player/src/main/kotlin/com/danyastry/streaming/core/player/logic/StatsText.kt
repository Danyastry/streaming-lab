package com.danyastry.streaming.core.player.logic

import com.danyastry.streaming.core.player.model.VideoRendition
import java.util.Locale

object StatsText {

    fun bitrate(bitsPerSecond: Long?): String = when {
        bitsPerSecond == null || bitsPerSecond <= 0 -> "-"
        bitsPerSecond >= MEGA -> String.format(Locale.US, "%.2f Mbit/s", bitsPerSecond / MEGA.toDouble())
        else -> "${bitsPerSecond / KILO} kbit/s"
    }

    fun seconds(millis: Long?): String =
        if (millis == null) "-" else String.format(Locale.US, "%.1f s", millis / MILLIS_PER_SECOND)

    fun bytes(count: Long): String = when {
        count >= MEGA_BYTES -> String.format(Locale.US, "%.1f MB", count / MEGA_BYTES.toDouble())
        count >= KILO_BYTES -> "${count / KILO_BYTES} KB"
        else -> "$count B"
    }

    fun rendition(rendition: VideoRendition?): String =
        if (rendition == null) "-" else "${rendition.height}p - ${bitrate(rendition.bitsPerSecond)}"

    private const val KILO = 1_000L
    private const val MEGA = 1_000_000L
    private const val KILO_BYTES = 1_024L
    private const val MEGA_BYTES = 1_048_576L
    private const val MILLIS_PER_SECOND = 1_000.0
}
