package com.tlc.domain.use_cases.customer

import com.tlc.domain.repository.firebase.ReservationRepository
import javax.inject.Inject

class ReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend fun loadReservations(placeId: String, date: String) =
        reservationRepository.getReservations(placeId, date)
}
