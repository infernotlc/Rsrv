package com.tlc.feature.feature.design.state

import com.tlc.domain.model.firebase.DesignItem

    data class DesignState(
        val designItems: List<DesignItem> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )