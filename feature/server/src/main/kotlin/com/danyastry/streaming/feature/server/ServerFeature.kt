package com.danyastry.streaming.feature.server

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danyastry.streaming.core.ui.FeatureCategory
import com.danyastry.streaming.core.ui.FeatureEntry
import com.danyastry.streaming.core.ui.FeatureInfo
import com.danyastry.streaming.feature.server.ui.ServerRoute

object ServerFeature : FeatureEntry {

    override val info = FeatureInfo(
        id = "stream-server",
        name = "Local stream server",
        category = FeatureCategory.PLAYBACK,
        summary = "The HLS and DASH files every lab plays, served from inside the app over a network you can slow down",
    )

    @Composable
    override fun Content(modifier: Modifier) {
        ServerRoute(modifier = modifier)
    }
}
