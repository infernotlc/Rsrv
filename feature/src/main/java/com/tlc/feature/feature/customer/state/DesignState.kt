package com.tlc.feature.feature.customer.state

import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.utils.RootResult

data class DesignState(
    val isLoading: Boolean = false,
    val result: RootResult<List<DesignItem>>? = null
)