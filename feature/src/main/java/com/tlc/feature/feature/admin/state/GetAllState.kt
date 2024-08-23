package com.tlc.feature.feature.admin.state

import com.tlc.domain.model.firebase.PlaceData
import com.tlc.domain.utils.RootResult

data class GetAllState(
    val isLoading: Boolean = false,
    val places: List<PlaceData> = emptyList(),
    val result: RootResult<List<PlaceData>>? = null,
    val error: String? = null
)