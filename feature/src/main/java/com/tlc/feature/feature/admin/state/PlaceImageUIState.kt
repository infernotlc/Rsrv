package com.tlc.feature.feature.admin.state

data class PlaceImageUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val imageUri: String? = null
)
