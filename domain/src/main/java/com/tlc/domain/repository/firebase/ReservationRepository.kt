package com.tlc.domain.repository.firebase

import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    suspend fun getReservations(placeId: String, date: String): Flow<RootResult<List<Reservation>>>
    suspend fun saveReservation(placeId: String, reservation: Reservation): Flow<RootResult<Unit>>
}
