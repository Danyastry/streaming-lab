package com.danyastry.streaming.core.streams.host

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.danyastry.streaming.core.server.throttle.NetworkTraces
import com.danyastry.streaming.core.streams.model.LocalStreams
import com.google.common.truth.Truth.assertThat
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StreamHostTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        StreamHost.switchTo(context, NetworkTraces.UNLIMITED)
        StreamHost.log.clear()
    }

    @Test
    fun servesTheHlsMasterPlaylistFromAssets() {
        val connection = open(StreamHost.url(context, LocalStreams.VOD_HLS))

        assertThat(connection.responseCode).isEqualTo(200)
        assertThat(connection.contentType).isEqualTo("application/vnd.apple.mpegurl")
        assertThat(connection.inputStream.use { it.readBytes() }.decodeToString()).startsWith("#EXTM3U")
    }

    @Test
    fun servesTheDashManifestFromAssets() {
        val connection = open(StreamHost.url(context, LocalStreams.VOD_DASH))

        assertThat(connection.responseCode).isEqualTo(200)
        assertThat(connection.contentType).isEqualTo("application/dash+xml")
        assertThat(connection.inputStream.use { it.readBytes() }.decodeToString()).contains("<MPD")
    }

    @Test
    fun servesAByteRangeOfASegment() {
        val connection = open(StreamHost.start(context).baseUrl + "vod/init-0.m4s")
        connection.setRequestProperty("Range", "bytes=4-7")

        assertThat(connection.responseCode).isEqualTo(206)
        assertThat(connection.inputStream.use { it.readBytes() }.decodeToString()).isEqualTo("ftyp")
    }

    @Test
    fun logsEveryRequest() {
        open(StreamHost.url(context, LocalStreams.VOD_HLS)).inputStream.use { it.readBytes() }
        open(StreamHost.start(context).baseUrl + "vod/missing.m4s").responseCode

        val records = StreamHost.log.records.value
        assertThat(records.map { it.path to it.status })
            .containsExactly("vod/missing.m4s" to 404, "vod/master.m3u8" to 200)
            .inOrder()
    }

    private fun open(url: String) = URL(url).openConnection() as HttpURLConnection
}
