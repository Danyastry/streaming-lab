plugins {
    alias(libs.plugins.streaming.android.library.compose)
}

android {
    namespace = "com.danyastry.streaming.core.player"
}

dependencies {
    api(libs.androidx.media3.exoplayer)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.material3)

    androidTestImplementation(projects.core.streams)
}
