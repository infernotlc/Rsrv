package com.tlc.data.ui.repository.firebase

import android.util.Log
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

        override suspend fun saveReservation(placeId: String, reservations: List<Reservation>): Result<Unit> {
            return try {
                val adminUserId = getAdminUserIdByPlace(placeId)
                    ?: return Result.failure(Exception("Admin User ID not found for this place"))

                val reservationRef = firestore.collection("users")
                    .document(adminUserId)
                    .collection("places")
                    .document(placeId)
                    .collection("reservations")

                reservations.forEach { reservation ->
                    val newDocRef = reservationRef.document()
                    newDocRef.set(reservation).await()
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        private suspend fun getAdminUserIdByPlace(placeId: String): String? {
            return try {
                val usersSnapshot = firestore.collection("users")
                    .whereEqualTo("role", "admin") // Get only admin users
                    .get()
                    .await()

                Log.d("getAdminUserIdByPlace", "Admin users found: ${usersSnapshot.size()}")

                for (userDoc in usersSnapshot.documents) {
                    val placesSnapshot = firestore.collection("users")
                        .document(userDoc.id)
                        .collection("places")
                        .whereEqualTo("id", placeId)
                        .get()
                        .await()

                    Log.d(
                        "getAdminUserIdByPlace",
                        "Places found for user ${userDoc.id}: ${placesSnapshot.size()}"
                    )

                    if (!placesSnapshot.isEmpty) {
                        return userDoc.id
                    }
                }

                null
            } catch (e: Exception) {
                Log.e("getAdminUserIdByPlace", "Error fetching admin user ID", e)
                null
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
            Log.e(
                "ReservationRepository",
                "Error cancelling unapproved reservations: ${e.message}",
                e
            )
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
