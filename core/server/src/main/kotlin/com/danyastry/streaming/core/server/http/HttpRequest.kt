package com.danyastry.streaming.core.server.http

import java.io.InputStream

data class HttpRequest(
    val method: String,
    val path: String,
    val headers: Map<String, String>,
) {
    fun header(name: String): String? = headers[name.lowercase()]

    companion object {
        fun read(input: InputStream): HttpRequest? {
            val requestLine = readLine(input) ?: return null
            val parts = requestLine.split(' ')
            if (parts.size < 3) return null
            val headers = mutableMapOf<String, String>()
            while (true) {
                val line = readLine(input) ?: return null
                if (line.isEmpty()) break
                val colon = line.indexOf(':')
                if (colon > 0) headers[line.substring(0, colon).trim().lowercase()] = line.substring(colon + 1).trim()
            }
            return HttpRequest(parts[0].uppercase(), parts[1].substringBefore('?'), headers)
        }

        private fun readLine(input: InputStream): String? {
            val line = StringBuilder()
            while (true) {
                val next = input.read()
                if (next == -1) return if (line.isEmpty()) null else line.toString()
                if (next == '\n'.code) return line.toString().removeSuffix("\r")
                if (line.length >= MAX_LINE) return null
                line.append(next.toChar())
            }
        }

        private const val MAX_LINE = 8 * 1024
    }
}
