package com.danyastry.streaming.core.ui.environment

data class RunEnvironment(
    val isEmulator: Boolean,
    val isDebuggable: Boolean,
) {
    companion object {
        fun detect(fingerprint: String, hardware: String, product: String, debuggable: Boolean): RunEnvironment {
            val emulator = fingerprint.startsWith("generic") || fingerprint.contains("emulator") ||
                hardware == "ranchu" || hardware == "goldfish" || product.startsWith("sdk_") || product.contains("emulator")
            return RunEnvironment(isEmulator = emulator, isDebuggable = debuggable)
        }
    }
}
