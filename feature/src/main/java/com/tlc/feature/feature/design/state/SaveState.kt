package com.tlc.feature.feature.design.state

data class SaveState(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)