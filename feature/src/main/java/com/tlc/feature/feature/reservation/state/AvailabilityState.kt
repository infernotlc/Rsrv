package com.tlc.feature.feature.reservation.state

import com.tlc.domain.utils.RootResult

data class AvailabilityState(
    val isLoading: Boolean = false,
    val result: RootResult<Boolean> = RootResult.Loading
)