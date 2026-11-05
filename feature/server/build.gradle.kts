plugins {
    alias(libs.plugins.streaming.android.feature)
}

android {
    namespace = "com.danyastry.streaming.feature.server"
}

dependencies {
    implementation(projects.core.streams)
}
