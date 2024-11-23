package com.tlc.domain.use_cases.customer

import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import javax.inject.Inject

class SaveReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend fun saveReservation(placeId: String, reservation: Reservation) =
        reservationRepository.saveReservation(placeId, reservation)
}
