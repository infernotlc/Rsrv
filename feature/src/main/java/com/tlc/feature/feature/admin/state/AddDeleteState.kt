package com.tlc.feature.feature.admin.state

import com.tlc.domain.utils.RootResult

data class AddDeleteState(
    val isLoading: Boolean = false,
    val result: RootResult<Boolean>? = null,
    val error: String? = null
)

