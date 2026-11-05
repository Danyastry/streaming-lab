package com.danyastry.streaming.core.ui.environment

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RunEnvironmentTest {

    @Test
    fun `recognises Android emulators`() {
        assertThat(RunEnvironment.detect("google/sdk_gphone64_arm64/emu64a:16/BP22", "ranchu", "sdk_gphone64_arm64", false).isEmulator).isTrue()
        assertThat(RunEnvironment.detect("generic/sdk/generic:8.0.0", "goldfish", "sdk", false).isEmulator).isTrue()
    }

    @Test
    fun `treats phones as real devices`() {
        val pixel = RunEnvironment.detect("google/husky/husky:15/AP4A/release-keys", "husky", "husky", debuggable = true)

        assertThat(pixel.isEmulator).isFalse()
        assertThat(pixel.isDebuggable).isTrue()
    }
}
