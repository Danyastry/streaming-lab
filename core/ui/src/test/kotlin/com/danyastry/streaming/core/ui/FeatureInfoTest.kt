package com.danyastry.streaming.core.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class FeatureInfoTest {

    @Test
    fun `accepts kebab-case ids`() {
        assertThat(info(id = "startup").id).isEqualTo("startup")
        assertThat(info(id = "cold-start").id).isEqualTo("cold-start")
    }

    @Test
    fun `rejects ids that are not lowercase kebab-case`() {
        listOf("", "STARTUP", "cold_start", "cold start", "-cold", "cold-", "cold--start").forEach { id ->
            assertThrows(IllegalArgumentException::class.java) { info(id = id) }
        }
    }

    @Test
    fun `rejects blank names and minSdk below the baseline`() {
        assertThrows(IllegalArgumentException::class.java) { info(name = " ") }
        assertThrows(IllegalArgumentException::class.java) { info(minSdk = 25) }
    }

    @Test
    fun `defaults to the app baseline`() {
        assertThat(info().minSdk).isEqualTo(AndroidVersion.BASELINE_SDK)
    }

    @Test
    fun `is supported from its minSdk up`() {
        val hce = info(minSdk = 31)

        assertThat(hce.isSupportedOn(30)).isFalse()
        assertThat(hce.isSupportedOn(31)).isTrue()
        assertThat(hce.isSupportedOn(35)).isTrue()
    }

    private fun info(id: String = "startup", name: String = "Startup", minSdk: Int = AndroidVersion.BASELINE_SDK) =
        FeatureInfo(id = id, name = name, category = FeatureCategory.PLAYBACK, summary = "Summary", minSdk = minSdk)
}
