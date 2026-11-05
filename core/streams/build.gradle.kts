plugins {
    alias(libs.plugins.streaming.android.library)
}

android {
    namespace = "com.danyastry.streaming.core.streams"

    androidResources {
        noCompress += listOf("m4s", "m3u8", "mpd")
    }
}

dependencies {
    api(projects.core.server)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
