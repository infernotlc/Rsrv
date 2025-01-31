package com.tlc.domain.use_cases.reservation

import com.tlc.domain.repository.firebase.ReservationRepository
import javax.inject.Inject

class CancelAllReservationsUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    suspend operator fun invoke(placeId : String) = reservationRepository.cancelAllReservations(placeId)
}
