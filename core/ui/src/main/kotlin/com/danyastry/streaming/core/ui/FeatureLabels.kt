package com.danyastry.streaming.core.ui

import androidx.annotation.StringRes

@get:StringRes
val FeatureCategory.titleRes: Int
    get() = when (this) {
        FeatureCategory.PLAYBACK -> R.string.ui_category_playback
        FeatureCategory.MANIFESTS -> R.string.ui_category_manifests
        FeatureCategory.ADAPTIVE -> R.string.ui_category_adaptive
        FeatureCategory.PLATFORM -> R.string.ui_category_platform
        FeatureCategory.OFFLINE -> R.string.ui_category_offline
    }

@get:StringRes
val FeatureCategory.descriptionRes: Int
    get() = when (this) {
        FeatureCategory.PLAYBACK -> R.string.ui_category_playback_description
        FeatureCategory.MANIFESTS -> R.string.ui_category_manifests_description
        FeatureCategory.ADAPTIVE -> R.string.ui_category_adaptive_description
        FeatureCategory.PLATFORM -> R.string.ui_category_platform_description
        FeatureCategory.OFFLINE -> R.string.ui_category_offline_description
    }
