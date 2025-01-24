package com.tlc.data.ui.repository.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReservationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ReservationRepository {

    override suspend fun saveReservation(
        chairId: String,
        tableId: String,
        customerId: String,
        date: String,
        time: String,
        placeId: String,
        isApproved: Boolean,
        timestamp: Timestamp
    ) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        Log.d("ReservationRepository", "Saving reservation for chairId: $chairId, tableId: $tableId")

        try {
            val reservation = mapOf(
                "chairId" to chairId,
                "tableId" to tableId,
                "customerId" to customerId,
                "date" to date,
                "time" to time,
                "isApproved" to isApproved,
                "timestamp" to timestamp
            )

            firestore.collection("users")
                .document(currentUserId)
                .collection("places")
                .document(placeId)
                .collection("reservations")
                .add(reservation)
                .await()

            Log.d("ReservationRepository", "Reservation saved successfully for chairId: $chairId")
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error saving reservation: ${e.message}", e)
            throw e
        }
    }

    override suspend fun cancelUnapprovedReservations(placeId: String) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        Log.d("ReservationRepository", "Cancelling unapproved reservations for placeId: $placeId")

        try {
            val snapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("places")
                .document(placeId)
                .collection("reservations")
                .whereEqualTo("isApproved", false)
                .get()
                .await()

            for (doc in snapshot.documents) {
                val timestamp = doc.getLong("timestamp") ?: 0L
                if (System.currentTimeMillis() - timestamp > 30 * 60 * 1000) {
                    doc.reference.delete().await()
                    Log.d("ReservationRepository", "Deleted reservation with timestamp: $timestamp")
                }
            }
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error cancelling unapproved reservations: ${e.message}", e)
            throw e
        }
    }

    override suspend fun cancelAllReservations(placeId: String) {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        Log.d("ReservationRepository", "Cancelling all reservations for placeId: $placeId")

        try {
            val snapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("places")
                .document(placeId)
                .collection("reservations")
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.reference.delete().await()
                Log.d("ReservationRepository", "Deleted reservation: ${doc.id}")
            }
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error cancelling all reservations: ${e.message}", e)
            throw e
        }
    }

    override suspend fun fetchReservationsForPlace(placeId: String): List<Reservation> {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        Log.d("ReservationRepository", "Fetching reservations for placeId: $placeId")

        return try {
            val snapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("places")
                .document(placeId)
                .collection("reservations")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Reservation::class.java).also {
                    Log.d("ReservationRepository", "Fetched reservation: ${doc.id}")
                }
            }
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error fetching reservations: ${e.message}", e)
            emptyList()
        }
    }
}
