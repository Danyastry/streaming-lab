package com.danyastry.streaming.core.server.content

fun interface StreamContent {
    fun read(path: String): ByteArray?
}
