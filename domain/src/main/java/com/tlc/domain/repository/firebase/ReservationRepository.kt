package com.tlc.domain.repository.firebase

import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Reservation
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    suspend fun saveReservation(
        placeId: String,
        reservations: List<Reservation>
    ): Result<Unit>

    suspend fun getReservations(placeId: String, date: String, onReservationsUpdated: (List<Reservation>) -> Unit)

    suspend fun getSavedReservationTimes(placeId: String): Flow<List<String>>

    suspend fun getReservedTimesFromFirestore(placeId: String, tableId: String, date:String): List<String>

    suspend fun markTableAsReserved(placeId: String, designId: String): Result<Unit>

    suspend fun cancelUnapprovedReservations(placeId: String)

    suspend fun cancelAllReservations(placeId: String)

    suspend fun fetchReservationsForPlace(placeId: String): List<Reservation>
}
