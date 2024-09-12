package com.tlc.domain.repository.firebase

import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp

interface MakeAReservationRepository {
    suspend fun updateReservationStatus(
    designItemId: String,
    isReserved: Boolean,
    chairId: String,
    reservedBy: String? = null,
    reservationStartTime: Timestamp? = null,
    reservationEndTime: Timestamp? = null
): Flow<RootResult<Unit>>

    suspend fun checkAvailability(
        designItemId: String,
        chairId: String,
        reservationStartTime: Timestamp,
        reservationEndTime: Timestamp
    ): Flow<RootResult<Boolean>>
}