package com.danyastry.streaming.feature.playback.ui

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.danyastry.streaming.core.server.throttle.NetworkTraces
import com.danyastry.streaming.core.streams.model.LocalStreams
import com.danyastry.streaming.feature.playback.data.FakePlaybackEngine
import com.danyastry.streaming.feature.playback.data.FakeStreamSource
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaybackViewModelTest {

    private val engine = FakePlaybackEngine()
    private val source = FakeStreamSource()

    @Test
    fun `starts the HLS stream on the current trace`() {
        val viewModel = PlaybackViewModel(engine, source)

        assertThat(engine.played).containsExactly("http://127.0.0.1:4321/vod/master.m3u8" to "application/x-mpegURL")
        assertThat(viewModel.state.value.trace).isEqualTo(NetworkTraces.FAST)
        assertThat(viewModel.state.value.url).isEqualTo("http://127.0.0.1:4321/vod/master.m3u8")
    }

    @Test
    fun `switching to DASH plays the manifest with the DASH mime type`() {
        val viewModel = PlaybackViewModel(engine, source)

        viewModel.onStreamChange(LocalStreams.VOD_DASH)

        assertThat(engine.played.last()).isEqualTo("http://127.0.0.1:4321/vod/manifest.mpd" to "application/dash+xml")
        assertThat(viewModel.state.value.stream).isEqualTo(LocalStreams.VOD_DASH)
    }

    @Test
    fun `changing the trace mid play does not restart playback`() {
        val viewModel = PlaybackViewModel(engine, source)

        viewModel.onTraceChange(NetworkTraces.DROP)

        assertThat(engine.played).hasSize(1)
        assertThat(source.trace).isEqualTo(NetworkTraces.DROP)
        assertThat(viewModel.state.value.trace).isEqualTo(NetworkTraces.DROP)
    }

    @Test
    fun `restart resets the trace timeline before playing again`() {
        val viewModel = PlaybackViewModel(engine, source)
        viewModel.onTraceChange(NetworkTraces.OSCILLATING)
        source.switched.clear()

        viewModel.onRestart()

        assertThat(source.switched).containsExactly(NetworkTraces.OSCILLATING)
        assertThat(engine.played).hasSize(2)
    }

    @Test
    fun `stop pauses and clearing releases the player`() {
        val store = ViewModelStore()
        val owner = object : ViewModelStoreOwner {
            override val viewModelStore = store
        }
        val factory = viewModelFactory { initializer { PlaybackViewModel(engine, source) } }
        val viewModel = ViewModelProvider(owner, factory)[PlaybackViewModel::class.java]

        viewModel.onStop()
        store.clear()

        assertThat(engine.pauses).isEqualTo(1)
        assertThat(engine.released).isTrue()
    }
}
