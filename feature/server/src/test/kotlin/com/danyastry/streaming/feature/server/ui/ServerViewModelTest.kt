package com.danyastry.streaming.feature.server.ui

import com.danyastry.streaming.core.server.log.RequestRecord
import com.danyastry.streaming.core.server.throttle.NetworkTraces
import com.danyastry.streaming.feature.server.data.FakeStreamGateway
import com.danyastry.streaming.feature.server.model.FetchResult
import com.google.common.truth.Truth.assertThat
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServerViewModelTest {

    private val gateway = FakeStreamGateway()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts with the gateway address and trace`() {
        val state = ServerViewModel(gateway).state.value

        assertThat(state.baseUrl).isEqualTo("http://127.0.0.1:4321/")
        assertThat(state.trace).isEqualTo(NetworkTraces.UNLIMITED)
        assertThat(state.fetching).isNull()
    }

    @Test
    fun `changing the trace switches the server`() {
        val viewModel = ServerViewModel(gateway)

        viewModel.onTraceChange(NetworkTraces.SLOW)

        assertThat(gateway.switched).containsExactly(NetworkTraces.SLOW)
        assertThat(viewModel.state.value.trace).isEqualTo(NetworkTraces.SLOW)
    }

    @Test
    fun `fetch shows progress then the result`() {
        val viewModel = ServerViewModel(gateway)

        viewModel.onFetch("vod/master.m3u8")
        assertThat(viewModel.state.value.fetching).isEqualTo("vod/master.m3u8")

        val result = FetchResult("vod/master.m3u8", 200, "application/vnd.apple.mpegurl", 7, 3, "#EXTM3U")
        gateway.response.complete(result)

        assertThat(viewModel.state.value.fetching).isNull()
        assertThat(viewModel.state.value.fetched).isEqualTo(result)
    }

    @Test
    fun `second fetch is ignored while one is running`() {
        val viewModel = ServerViewModel(gateway)

        viewModel.onFetch("vod/master.m3u8")
        viewModel.onFetch("vod/manifest.mpd")

        assertThat(viewModel.state.value.fetching).isEqualTo("vod/master.m3u8")
    }

    @Test
    fun `failed fetch shows the error and keeps the last result`() {
        val viewModel = ServerViewModel(gateway)
        val first = FetchResult("vod/master.m3u8", 200, null, 7, 3, "#EXTM3U")
        gateway.response.complete(first)
        viewModel.onFetch("vod/master.m3u8")

        gateway.response = CompletableDeferred<FetchResult>().apply { completeExceptionally(IOException("Connection refused")) }
        viewModel.onFetch("vod/manifest.mpd")

        assertThat(viewModel.state.value.error).isEqualTo("Connection refused")
        assertThat(viewModel.state.value.fetched).isEqualTo(first)
        assertThat(viewModel.state.value.fetching).isNull()
    }

    @Test
    fun `clear empties the request log`() {
        gateway.log.value = listOf(RequestRecord("vod/master.m3u8", 200, null, 10, 0, 1, "Unlimited"))
        val viewModel = ServerViewModel(gateway)

        viewModel.onClearLog()

        assertThat(viewModel.requests.value).isEmpty()
    }
}
