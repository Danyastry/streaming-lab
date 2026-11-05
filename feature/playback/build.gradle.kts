plugins {
    alias(libs.plugins.streaming.android.feature)
}

android {
    namespace = "com.danyastry.streaming.feature.playback"
}

dependencies {
    implementation(projects.core.player)
    implementation(projects.core.streams)
}
