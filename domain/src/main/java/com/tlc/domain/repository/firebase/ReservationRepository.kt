package com.tlc.domain.repository.firebase

import com.tlc.domain.model.firebase.Reservation

interface ReservationRepository {
    suspend fun saveReservation(
        placeId: String,
        reservations: List<Reservation>
    ): Result<Unit>

    suspend fun getReservations(placeId: String): List<Reservation>

    suspend fun cancelUnapprovedReservations(placeId: String)

    suspend fun cancelAllReservations(placeId: String)

    suspend fun fetchReservationsForPlace(placeId: String): List<Reservation>
}
