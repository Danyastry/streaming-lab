package com.danyastry.streaming.core.player.data

import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.danyastry.streaming.core.player.model.PlaybackPhase
import com.danyastry.streaming.core.player.model.PlaybackStats
import com.danyastry.streaming.core.player.model.SwitchReason
import com.danyastry.streaming.core.server.throttle.NetworkTraces
import com.danyastry.streaming.core.streams.host.StreamHost
import com.danyastry.streaming.core.streams.model.LocalStream
import com.danyastry.streaming.core.streams.model.LocalStreams
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExoPlaybackEngineTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private lateinit var engine: ExoPlaybackEngine

    @After
    fun tearDown() {
        instrumentation.runOnMainSync { engine.release() }
    }

    @Test
    fun playsTheHlsStream() = assertPlays(LocalStreams.VOD_HLS, MimeTypes.APPLICATION_M3U8)

    @Test
    fun playsTheDashStream() = assertPlays(LocalStreams.VOD_DASH, MimeTypes.APPLICATION_MPD)

    @Test
    fun playingAgainBuildsAFreshPlayer() {
        assertPlays(LocalStreams.VOD_HLS, MimeTypes.APPLICATION_M3U8)
        val first = engine.player.value
        val url = StreamHost.url(context, LocalStreams.VOD_DASH)

        instrumentation.runOnMainSync { engine.play(url, MimeTypes.APPLICATION_MPD) }

        val second = engine.player.value
        assertThat(second).isNotNull()
        assertThat(second).isNotSameInstanceAs(first)
        assertThat(engine.stats.value.segments).isEqualTo(0)
        instrumentation.runOnMainSync { assertThat(first!!.playbackState).isEqualTo(Player.STATE_IDLE) }
    }

    private fun assertPlays(stream: LocalStream, mimeType: String) {
        StreamHost.switchTo(context, NetworkTraces.UNLIMITED)
        val url = StreamHost.url(context, stream)
        instrumentation.runOnMainSync {
            if (!::engine.isInitialized) engine = ExoPlaybackEngine(context)
            engine.play(url, mimeType)
            engine.player.value!!.volume = 0f
        }

        val stats = awaitStats { it.phase == PlaybackPhase.READY && it.playing != null && it.positionMillis > 0 }

        assertThat(stats.error).isNull()
        assertThat(stats.startupMillis).isNotNull()
        assertThat(stats.segments).isAtLeast(1)
        assertThat(stats.switches.first().reason).isEqualTo(SwitchReason.INITIAL)
        assertThat(stats.playing!!.height).isIn(listOf(216, 360, 540, 720))
    }

    private fun awaitStats(condition: (PlaybackStats) -> Boolean): PlaybackStats {
        val deadline = System.currentTimeMillis() + TIMEOUT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            val stats = engine.stats.value
            if (stats.error != null || condition(stats)) return stats.also { assertWithMessage("error").that(it.error).isNull() }
            Thread.sleep(POLL_MILLIS)
        }
        throw AssertionError("Timed out, last stats: ${engine.stats.value}")
    }

    private companion object {
        const val TIMEOUT_MILLIS = 15_000L
        const val POLL_MILLIS = 100L
    }
}
