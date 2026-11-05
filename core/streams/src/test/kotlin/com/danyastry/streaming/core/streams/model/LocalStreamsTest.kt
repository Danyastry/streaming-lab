package com.danyastry.streaming.core.streams.model

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

class LocalStreamsTest {

    private val assets = File("src/main/assets")

    @Test
    fun `every listed stream exists in the assets`() {
        LocalStreams.ALL.forEach { stream -> assertThat(File(assets, stream.path).isFile).isTrue() }
    }

    @Test
    fun `every segment the manifests name is in the assets`() {
        val vod = File(assets, "vod")
        val playlists = vod.listFiles { file -> file.name.startsWith("media_") }!!.toList()
        val named = playlists.flatMap { playlist ->
            playlist.readLines().filter { it.isNotBlank() && !it.startsWith("#") } +
                Regex("URI=\"([^\"]+)\"").findAll(playlist.readText()).map { it.groupValues[1] }
        }

        assertThat(playlists).hasSize(5)
        assertThat(named.size).isAtLeast(5 * 11)
        named.forEach { name -> assertThat(File(vod, name).isFile).isTrue() }
    }

    @Test
    fun `every HLS playlist declares independent segments so players can switch down on an empty buffer`() {
        val playlists = File(assets, "vod").listFiles { file -> file.extension == "m3u8" }!!.toList()

        assertThat(playlists).hasSize(6)
        playlists.forEach { playlist -> assertThat(playlist.readLines()).contains("#EXT-X-INDEPENDENT-SEGMENTS") }
    }
}
