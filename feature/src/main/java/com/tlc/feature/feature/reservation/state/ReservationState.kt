package com.tlc.feature.feature.reservation.state

import com.tlc.domain.utils.RootResult

data class ReservationState(
    val isLoading: Boolean = false,
    val result: RootResult<Unit> = RootResult.Loading
)