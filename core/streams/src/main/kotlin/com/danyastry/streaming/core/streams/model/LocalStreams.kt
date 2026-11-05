package com.danyastry.streaming.core.streams.model

object LocalStreams {
    val VOD_HLS = LocalStream("Test pattern, 4 renditions", StreamFormat.HLS, "vod/master.m3u8")
    val VOD_DASH = LocalStream("Test pattern, 4 renditions", StreamFormat.DASH, "vod/manifest.mpd")

    val ALL: List<LocalStream> = listOf(VOD_HLS, VOD_DASH)
}
