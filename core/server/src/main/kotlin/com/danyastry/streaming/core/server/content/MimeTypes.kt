package com.danyastry.streaming.core.server.content

object MimeTypes {

    fun of(path: String): String = when (path.substringAfterLast('.', "").lowercase()) {
        "m3u8" -> "application/vnd.apple.mpegurl"
        "mpd" -> "application/dash+xml"
        "m4s" -> "video/iso.segment"
        "mp4" -> "video/mp4"
        "vtt" -> "text/vtt"
        "json" -> "application/json"
        else -> "application/octet-stream"
    }
}
