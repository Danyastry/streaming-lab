package com.danyastry.streaming.core.ui.environment

import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danyastry.streaming.core.ui.R

@Composable
fun rememberRunEnvironment(): RunEnvironment {
    val context = LocalContext.current
    return remember(context) {
        RunEnvironment.detect(
            fingerprint = Build.FINGERPRINT,
            hardware = Build.HARDWARE,
            product = Build.PRODUCT,
            debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
        )
    }
}

@Composable
fun EnvironmentBanner(modifier: Modifier = Modifier) {
    val environment = rememberRunEnvironment()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(if (environment.isEmulator) R.string.ui_environment_emulator else R.string.ui_environment_device),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (environment.isDebuggable) {
                Text(
                    text = stringResource(R.string.ui_environment_debuggable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
