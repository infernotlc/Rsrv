package com.tlc.domain.use_cases.reservation

import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserReservationsUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    suspend operator fun invoke(userId: String): Flow<List<Reservation>> {
        return repository.getUserReservations(userId)
    }
} 