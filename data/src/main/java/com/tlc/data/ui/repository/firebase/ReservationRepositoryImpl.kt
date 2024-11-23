package com.tlc.data.ui.repository.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReservationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ReservationRepository {

    override suspend fun getReservations(
        placeId: String, date: String
    ): Flow<RootResult<List<Reservation>>> = flow {
        emit(RootResult.Loading)
        try {
            val userId = firebaseAuth.currentUser?.uid ?: throw Exception("User not logged in")
            val snapshot = firestore.collection("users")
                .document(userId).collection("places")
                .document(placeId).collection("reservations")
                .whereEqualTo("date", date)
                .get().await()

            val reservations = snapshot.documents.mapNotNull { it.toObject(Reservation::class.java) }
            emit(RootResult.Success(reservations))
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Failed to load reservations"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun saveReservation(
        placeId: String, reservation: Reservation
    ): Flow<RootResult<Unit>> = flow {
        emit(RootResult.Loading)
        try {
            val userId = firebaseAuth.currentUser?.uid ?: throw Exception("User not logged in")
            firestore.collection("users").document(userId)
                .collection("places").document(placeId)
                .collection("reservations").add(reservation).await()
            emit(RootResult.Success(Unit))
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Failed to save reservation"))
        }
    }.flowOn(Dispatchers.IO)
}
