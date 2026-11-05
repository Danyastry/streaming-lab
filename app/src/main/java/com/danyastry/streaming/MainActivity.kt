package com.danyastry.streaming

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.danyastry.streaming.catalog.FeatureCatalog
import com.danyastry.streaming.catalog.FeatureRegistry
import com.danyastry.streaming.core.ui.theme.StreamingTheme
import com.danyastry.streaming.navigation.StreamingNavHost

@OptIn(ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {

    private val catalog = FeatureCatalog(FeatureRegistry.features, deviceSdk = Build.VERSION.SDK_INT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamingTheme {
                Box(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                    StreamingNavHost(catalog)
                }
            }
        }
    }
}
