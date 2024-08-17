package com.tlc.feature.feature.auth.forget_password.state

data class ForgotPasswordState(
    val transaction: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)