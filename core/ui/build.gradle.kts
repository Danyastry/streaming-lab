plugins {
    alias(libs.plugins.streaming.android.library.compose)
}

android {
    namespace = "com.danyastry.streaming.core.ui"
}

dependencies {
    api(libs.androidx.material3)
}
