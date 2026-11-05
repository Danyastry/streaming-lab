package com.danyastry.streaming.feature.server.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danyastry.streaming.core.server.log.RequestRecord
import com.danyastry.streaming.core.server.throttle.NetworkTrace
import com.danyastry.streaming.core.server.throttle.NetworkTraces
import com.danyastry.streaming.core.streams.model.LocalStreams
import com.danyastry.streaming.feature.server.R
import com.danyastry.streaming.feature.server.data.HostStreamGateway
import com.danyastry.streaming.feature.server.model.FetchResult

@Composable
fun ServerRoute(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val viewModel: ServerViewModel = viewModel { ServerViewModel(HostStreamGateway(context)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val requests by viewModel.requests.collectAsStateWithLifecycle()
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.server_intro), style = MaterialTheme.typography.bodyMedium)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.server_address), style = MaterialTheme.typography.labelSmall)
                Text(state.baseUrl, fontFamily = FontFamily.Monospace)
            }
        }
        Text(stringResource(R.string.server_network), style = MaterialTheme.typography.titleSmall)
        TracePicker(state.trace, viewModel::onTraceChange)
        Text(stringResource(R.string.server_streams), style = MaterialTheme.typography.titleSmall)
        LocalStreams.ALL.forEach { stream ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${stream.format.name}: ${stream.path}", modifier = Modifier.weight(1f), fontFamily = FontFamily.Monospace)
                OutlinedButton(onClick = { viewModel.onFetch(stream.path) }, enabled = state.fetching == null) {
                    Text(stringResource(R.string.server_fetch))
                }
            }
        }
        state.error?.let { Text(stringResource(R.string.server_error, it), color = MaterialTheme.colorScheme.error) }
        state.fetched?.let { FetchedCard(it) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.server_log_title, requests.size), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = viewModel::onClearLog) { Text(stringResource(R.string.server_clear)) }
        }
        if (requests.isEmpty()) Text(stringResource(R.string.server_log_empty), style = MaterialTheme.typography.bodySmall)
        requests.take(LOG_ROWS).forEach { RequestRow(it) }
    }
}

@Composable
private fun TracePicker(selected: NetworkTrace, onSelect: (NetworkTrace) -> Unit) {
    Column {
        NetworkTraces.ALL.forEach { trace ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().selectable(selected = trace == selected, role = Role.RadioButton) { onSelect(trace) },
            ) {
                RadioButton(selected = trace == selected, onClick = null)
                Text(trace.name, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun FetchedCard(result: FetchResult) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(R.string.server_fetched, result.status, result.contentType.orEmpty(), result.bytes, result.millis),
                style = MaterialTheme.typography.labelSmall,
            )
            SelectionContainer {
                Text(
                    result.text,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun RequestRow(record: RequestRecord) {
    val rate = record.kilobitsPerSecond?.let { stringResource(R.string.server_rate, it) } ?: stringResource(R.string.server_rate_unknown)
    Text(
        stringResource(R.string.server_log_row, record.status, record.path, record.bytes, record.durationMillis, rate),
        fontFamily = FontFamily.Monospace,
        style = MaterialTheme.typography.bodySmall,
    )
}

private const val LOG_ROWS = 40
