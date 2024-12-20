package com.tlc.feature.feature.auth.login.state

data class IsLoggedInState(
    val transaction: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: String? = null
)