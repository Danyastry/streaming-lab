package com.danyastry.streaming.feature.playback

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danyastry.streaming.core.ui.FeatureCategory
import com.danyastry.streaming.core.ui.FeatureEntry
import com.danyastry.streaming.core.ui.FeatureInfo
import com.danyastry.streaming.feature.playback.ui.PlaybackRoute

object PlaybackFeature : FeatureEntry {

    override val info = FeatureInfo(
        id = "player-stats",
        name = "Player and stats",
        category = FeatureCategory.PLAYBACK,
        summary = "ExoPlayer on the local HLS and DASH streams with live stats: startup, stalls, buffer and every rendition switch",
    )

    @Composable
    override fun Content(modifier: Modifier) {
        PlaybackRoute(modifier = modifier)
    }
}
