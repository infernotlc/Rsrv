package com.tlc.data.ui.repository.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.domain.repository.firebase.MakeAReservationRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.sql.Timestamp
import javax.inject.Inject

class MakeAReservationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : MakeAReservationRepository {

    override suspend fun updateReservationStatus(
        designItemId: String,
        isReserved: Boolean,
        chairId: String,
        reservedBy: String?,
        reservationStartTime: Timestamp?,
        reservationEndTime: Timestamp?
    ): Flow<RootResult<Unit>> = flow {
        try {
            val chairRef = firestore.collection("design").document(designItemId)
                .collection("chairs").document(chairId)
            chairRef.update(
                "isReserved", isReserved,
                "reservedBy", reservedBy,
                "reservationStartTime", reservationStartTime,
                "reservationEndTime", reservationEndTime
            ).await()
            emit(RootResult.Success(Unit))
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Failed to update reservation"))
        }
    }

    override suspend fun checkAvailability(
        designItemId: String,
        chairId: String, // New parameter
        reservationStartTime: Timestamp,
        reservationEndTime: Timestamp
    ): Flow<RootResult<Boolean>> = flow {
        try {
            val chairSnapshot = firestore.collection("design").document(designItemId)
                .collection("design").document(chairId)
                .get()
                .await()

            if (chairSnapshot.exists()) {
                val currentReservationStart = chairSnapshot.getTimestamp("reservationStartTime")
                val currentReservationEnd = chairSnapshot.getTimestamp("reservationEndTime")

                val isAvailable = currentReservationEnd == null || reservationEndTime.before(
                    currentReservationStart?.toDate()
                ) || reservationStartTime.after(currentReservationEnd.toDate())

                emit(RootResult.Success(isAvailable))
            } else {
                emit(RootResult.Error("Chair not found"))
            }
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Failed to check availability"))
        }
    }
}