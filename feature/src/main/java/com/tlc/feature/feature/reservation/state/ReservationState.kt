package com.tlc.feature.feature.reservation.state

data class ReservationState(
    val date: String = "",
    val time: String = "",
    val availableChairs: List<Chair> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val placeId: String = ""
)

data class Chair(
    val id: String,
    val tableId: String,
    val isReserved: Boolean = false
)

