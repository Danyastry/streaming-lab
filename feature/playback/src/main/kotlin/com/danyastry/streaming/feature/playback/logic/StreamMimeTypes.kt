package com.danyastry.streaming.feature.playback.logic

import androidx.media3.common.MimeTypes
import com.danyastry.streaming.core.streams.model.StreamFormat

object StreamMimeTypes {

    fun of(format: StreamFormat): String = when (format) {
        StreamFormat.HLS -> MimeTypes.APPLICATION_M3U8
        StreamFormat.DASH -> MimeTypes.APPLICATION_MPD
    }
}
