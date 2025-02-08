package com.tlc.domain.repository.firebase

import com.tlc.domain.model.firebase.Reservation
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    suspend fun saveReservation(
        placeId: String,
        reservations: List<Reservation>
    ): Result<Unit>

    suspend fun getReservations(placeId: String, onReservationsUpdated: (List<Reservation>) -> Unit)

    suspend fun getReservationTimes(placeId: String): Flow<List<String>>

    suspend fun cancelUnapprovedReservations(placeId: String)

    suspend fun cancelAllReservations(placeId: String)

    suspend fun fetchReservationsForPlace(placeId: String): List<Reservation>
}
