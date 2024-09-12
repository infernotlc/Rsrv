package com.tlc.domain.use_cases.reservation

import com.tlc.domain.repository.firebase.MakeAReservationRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp
import javax.inject.Inject

class MakeAReservationUseCase @Inject constructor(
    private val designRepository: MakeAReservationRepository
) {
    suspend fun makeReservation(
        designItemId: String,
        isReserved: Boolean,
        chairId: String,
        reservedBy: String?,
        reservationStartTime: Timestamp?,
        reservationEndTime: Timestamp?
    ) =
        designRepository.updateReservationStatus(
            designItemId,
            isReserved,
            chairId ,
            reservedBy,
            reservationStartTime,
            reservationEndTime
        )


    suspend fun checkAvailability(
        designItemId: String,
        chairId: String,
        reservationStartTime: Timestamp,
        reservationEndTime: Timestamp
    ) =
        designRepository.checkAvailability(
            designItemId,
            chairId,
            reservationStartTime,
            reservationEndTime
        )
}
