package com.tlc.domain.repository.firebase

import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Place
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    suspend fun saveReservation(
        placeId: String,
        reservation: Reservation
    ): Result<Unit>

    suspend fun getReservations(placeId: String, date: String): Flow<List<Reservation>>

    suspend fun getSavedReservationTimes(placeId: String): Flow<List<String>>

    suspend fun getReservedTimesFromFirestore(placeId: String, tableId: String, date:String): List<String>

    suspend fun markTableAsReserved(placeId: String, designId: String): Result<Unit>

    suspend fun getUserReservations(userId: String): Flow<List<Reservation>>

    suspend fun fetchReservationsForPlace(placeId: String): List<Reservation>

    suspend fun getAdminPlaces(adminUserId: String): Flow<List<Place>>

    suspend fun getAllAdminReservations(adminUserId: String): Flow<List<Reservation>>

    suspend fun cancelReservation(reservationId: String, userId: String): Result<Unit>
}

