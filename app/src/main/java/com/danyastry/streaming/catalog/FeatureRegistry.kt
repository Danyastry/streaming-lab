package com.danyastry.streaming.catalog

import com.danyastry.streaming.core.ui.FeatureEntry
import com.danyastry.streaming.feature.playback.PlaybackFeature
import com.danyastry.streaming.feature.server.ServerFeature

object FeatureRegistry {
    val features: List<FeatureEntry> = listOf(
        ServerFeature,
        PlaybackFeature,
    )
}
