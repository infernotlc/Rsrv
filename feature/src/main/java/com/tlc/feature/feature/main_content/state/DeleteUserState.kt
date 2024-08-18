package com.tlc.feature.feature.main_content.state

data class DeleteUserState(
    val isLoading: Boolean = false,
    val transaction: Boolean = false,
    val error: String? = null
)