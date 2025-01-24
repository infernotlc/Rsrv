package com.tlc.domain.repository.firebase

import com.google.firebase.Timestamp
import com.tlc.domain.model.firebase.Reservation

interface ReservationRepository {
    suspend fun saveReservation(
        chairId: String,
        tableId: String,
        customerId: String,
        date: String,
        time: String,
        placeId: String,
        isApproved: Boolean,
        timestamp: Timestamp
    )

    suspend fun cancelUnapprovedReservations(placeId: String)

    suspend fun cancelAllReservations(placeId: String)

    suspend fun fetchReservationsForPlace(placeId: String): List<Reservation>
}
