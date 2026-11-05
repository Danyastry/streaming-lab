package com.danyastry.streaming.core.player.model

data class RenditionSwitch(
    val positionMillis: Long,
    val mediaMillis: Long,
    val from: VideoRendition?,
    val to: VideoRendition,
    val reason: SwitchReason,
)
