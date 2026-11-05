package com.danyastry.streaming.catalog

import com.danyastry.streaming.core.ui.FeatureCategory
import com.danyastry.streaming.core.ui.FeatureEntry

data class CatalogSection(
    val category: FeatureCategory,
    val entries: List<FeatureEntry>,
)
