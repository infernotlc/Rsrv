package com.tlc.domain.use_cases.reservation

import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import javax.inject.Inject

class SaveReservationUseCase @Inject constructor(private val reservationRepository: ReservationRepository) {
    suspend operator fun invoke ( placeId: String,
                                  reservations: List<Reservation>) = reservationRepository.saveReservation(placeId, reservations)
}