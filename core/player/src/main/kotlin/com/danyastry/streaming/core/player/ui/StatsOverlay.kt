package com.danyastry.streaming.core.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.danyastry.streaming.core.player.R
import com.danyastry.streaming.core.player.logic.StatsText
import com.danyastry.streaming.core.player.model.PlaybackStats

@Composable
fun StatsOverlay(stats: PlaybackStats, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color.Black.copy(alpha = OVERLAY_ALPHA), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Line(stringResource(R.string.player_overlay_state, stats.phase.name, if (stats.isPlaying) "playing" else "paused"))
        Line(stringResource(R.string.player_overlay_playing, StatsText.rendition(stats.playing)))
        Line(stringResource(R.string.player_overlay_loading, StatsText.rendition(stats.loading)))
        Line(stringResource(R.string.player_overlay_buffer, StatsText.seconds(stats.bufferedMillis)))
        Line(stringResource(R.string.player_overlay_estimate, StatsText.bitrate(stats.bandwidthEstimate)))
        Line(stringResource(R.string.player_overlay_position, StatsText.seconds(stats.positionMillis)))
    }
}

@Composable
private fun Line(text: String) {
    Text(text, color = Color.White, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
}

private const val OVERLAY_ALPHA = 0.6f
