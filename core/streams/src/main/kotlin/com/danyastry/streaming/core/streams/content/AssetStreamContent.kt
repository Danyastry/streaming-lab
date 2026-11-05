package com.danyastry.streaming.core.streams.content

import android.content.res.AssetManager
import com.danyastry.streaming.core.server.content.StreamContent
import java.io.FileNotFoundException

class AssetStreamContent(private val assets: AssetManager) : StreamContent {
    override fun read(path: String): ByteArray? = try {
        assets.open(path).use { it.readBytes() }
    } catch (_: FileNotFoundException) {
        null
    }
}
