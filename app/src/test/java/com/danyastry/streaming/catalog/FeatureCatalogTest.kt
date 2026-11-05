package com.danyastry.streaming.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.danyastry.streaming.core.ui.FeatureCategory
import com.danyastry.streaming.core.ui.FeatureEntry
import com.danyastry.streaming.core.ui.FeatureInfo
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class FeatureCatalogTest {

    @Test
    fun `empty catalog has no sections`() {
        val catalog = FeatureCatalog(emptyList(), deviceSdk = 35)

        assertThat(catalog.isEmpty).isTrue()
        assertThat(catalog.sections).isEmpty()
    }

    @Test
    fun `groups features into sections ordered by category`() {
        val coldStart = feature("cold-start", "Cold start", FeatureCategory.PLAYBACK)
        val recomposition = feature("recomposition", "Recomposition", FeatureCategory.MANIFESTS)
        val art = feature("art-compilation", "ART compilation", FeatureCategory.OFFLINE)

        val catalog = FeatureCatalog(listOf(art, recomposition, coldStart), deviceSdk = 35)

        assertThat(catalog.sections.map { it.category }).containsExactly(
            FeatureCategory.PLAYBACK,
            FeatureCategory.MANIFESTS,
            FeatureCategory.OFFLINE,
        ).inOrder()
    }

    @Test
    fun `sorts features inside a section by name`() {
        val tracing = feature("startup-tracing", "Startup tracing", FeatureCategory.PLAYBACK)
        val coldStart = feature("cold-start", "Cold start", FeatureCategory.PLAYBACK)

        val catalog = FeatureCatalog(listOf(tracing, coldStart), deviceSdk = 35)

        assertThat(catalog.sections.single().entries).containsExactly(coldStart, tracing).inOrder()
    }

    @Test
    fun `features newer than the device are listed but not supported`() {
        val future = feature("future", "Future feature", FeatureCategory.PLATFORM, minSdk = 31)

        val catalog = FeatureCatalog(listOf(future), deviceSdk = 30)

        assertThat(catalog.sections.single().entries).containsExactly(future)
        assertThat(catalog.isSupported(future)).isFalse()
        assertThat(catalog.find("future")).isNull()
    }

    @Test
    fun `finds supported features by id`() {
        val future = feature("future", "Future feature", FeatureCategory.PLATFORM, minSdk = 31)

        val catalog = FeatureCatalog(listOf(future), deviceSdk = 31)

        assertThat(catalog.isSupported(future)).isTrue()
        assertThat(catalog.find("future")).isSameInstanceAs(future)
        assertThat(catalog.find("unknown")).isNull()
    }

    @Test
    fun `rejects duplicate ids`() {
        val first = feature("startup-tracing", "Startup tracing", FeatureCategory.PLAYBACK)
        val second = feature("startup-tracing", "Trace", FeatureCategory.PLAYBACK)

        val error = assertThrows(IllegalArgumentException::class.java) { FeatureCatalog(listOf(first, second), deviceSdk = 35) }
        assertThat(error).hasMessageThat().contains("startup-tracing")
    }

    @Test
    fun `registry has unique ids`() {
        FeatureCatalog(FeatureRegistry.features, deviceSdk = 35)
    }

    private fun feature(id: String, name: String, category: FeatureCategory, minSdk: Int = 26): FeatureEntry =
        object : FeatureEntry {
            override val info = FeatureInfo(id = id, name = name, category = category, summary = "$name summary", minSdk = minSdk)

            @Composable
            override fun Content(modifier: Modifier) = Unit
        }
}
