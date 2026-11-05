package com.danyastry.streaming.feature.playback.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danyastry.streaming.core.player.data.ExoPlaybackEngine
import com.danyastry.streaming.core.player.logic.StatsText
import com.danyastry.streaming.core.player.model.PlaybackStats
import com.danyastry.streaming.core.player.model.RenditionSwitch
import com.danyastry.streaming.core.player.ui.PlayerSurface
import com.danyastry.streaming.core.player.ui.StatsOverlay
import com.danyastry.streaming.core.server.throttle.NetworkTraces
import com.danyastry.streaming.core.streams.host.HostStreamSource
import com.danyastry.streaming.core.streams.model.LocalStreams
import com.danyastry.streaming.core.ui.components.SegmentedSelector
import com.danyastry.streaming.core.ui.components.Stat
import com.danyastry.streaming.feature.playback.R
import com.danyastry.streaming.feature.playback.state.PlaybackUiState

@Composable
fun PlaybackRoute(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val viewModel: PlaybackViewModel = viewModel { PlaybackViewModel(ExoPlaybackEngine(context), HostStreamSource(context)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val player by viewModel.player.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onStop() }
    Column(modifier.verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().aspectRatio(VIDEO_ASPECT)) {
            PlayerSurface(player, Modifier.fillMaxWidth().aspectRatio(VIDEO_ASPECT))
            StatsOverlay(stats, Modifier.align(Alignment.TopStart).padding(8.dp))
        }
        Controls(state, viewModel)
        StatsSection(stats)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Controls(state: PlaybackUiState, viewModel: PlaybackViewModel) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.playback_intro), style = MaterialTheme.typography.bodyMedium)
        SegmentedSelector(
            options = LocalStreams.ALL,
            selected = state.stream,
            label = { it.format.name },
            onSelect = viewModel::onStreamChange,
        )
        Text(stringResource(R.string.playback_network), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NetworkTraces.ALL.forEach { trace ->
                FilterChip(
                    selected = trace == state.trace,
                    onClick = { viewModel.onTraceChange(trace) },
                    label = { Text(trace.name) },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.url.orEmpty(), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = viewModel::onRestart) { Text(stringResource(R.string.playback_restart)) }
        }
    }
}

@Composable
private fun StatsSection(stats: PlaybackStats) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        stats.error?.let { Text(stringResource(R.string.playback_error, it), color = MaterialTheme.colorScheme.error) }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row {
                    Stat(stringResource(R.string.playback_stat_startup), StatsText.seconds(stats.startupMillis), Modifier.weight(1f))
                    Stat(stringResource(R.string.playback_stat_stalls), stats.stalls.toString(), Modifier.weight(1f))
                    Stat(stringResource(R.string.playback_stat_stalled), StatsText.seconds(stats.stalledMillis), Modifier.weight(1f))
                }
                Row {
                    Stat(stringResource(R.string.playback_stat_segments), stats.segments.toString(), Modifier.weight(1f))
                    Stat(stringResource(R.string.playback_stat_downloaded), StatsText.bytes(stats.loadedBytes), Modifier.weight(1f))
                    Stat(stringResource(R.string.playback_stat_dropped), stats.droppedFrames.toString(), Modifier.weight(1f))
                }
            }
        }
        Text(stringResource(R.string.playback_switches, stats.switches.size), style = MaterialTheme.typography.titleSmall)
        if (stats.switches.isEmpty()) Text(stringResource(R.string.playback_switches_empty), style = MaterialTheme.typography.bodySmall)
        stats.switches.asReversed().forEach { SwitchRow(it) }
        Text(stringResource(R.string.playback_note), style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
    }
}

@Composable
private fun SwitchRow(switch: RenditionSwitch) {
    Text(
        stringResource(
            R.string.playback_switch_row,
            StatsText.seconds(switch.positionMillis),
            StatsText.seconds(switch.mediaMillis),
            switch.from?.let { "${it.height}p" } ?: "-",
            "${switch.to.height}p",
            switch.reason.name,
        ),
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodySmall,
    )
}

private const val VIDEO_ASPECT = 16f / 9f
