package com.tlc.feature.feature.customer.state

import com.tlc.domain.model.firebase.Place
import com.tlc.domain.utils.RootResult

data class GetAllState(
    val isLoading: Boolean = false,
    val result: RootResult<List<Place>>? = null
)

