package com.tlc.feature.feature.customer.reservation.state

import com.tlc.domain.model.firebase.Reservation

data class ReservationState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
