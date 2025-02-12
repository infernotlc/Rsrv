package com.tlc.data.ui.repository.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReservationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ReservationRepository {

    override suspend fun saveReservation(
        placeId: String,
        reservations: List<Reservation>
    ): Result<Unit> {
        return try {
            val adminUserId = getAdminUserIdByPlace(placeId)
                ?: return Result.failure(Exception("Admin User ID not found for this place"))

            reservations.forEach { reservation ->
                val tableRef = firestore.collection("users")
                    .document(adminUserId)
                    .collection("places")
                    .document(placeId)
                    .collection("design")
                    .document(reservation.tableId)
                    .collection("reservations")
                    .document(reservation.date)  // Group by date
                    .collection("times")
                    .document()

                tableRef.set(reservation).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReservations(
        placeId: String,
        date: String,
        onReservationsUpdated: (List<Reservation>) -> Unit
    ) {
        try {
            val adminUserId = getAdminUserIdByPlace(placeId) ?: return
            val reservationRef = firestore.collection("users")
                .document(adminUserId)
                .collection("places")
                .document(placeId)
                .collection("design")

            reservationRef.get().addOnSuccessListener { snapshot ->
                val reservations = mutableListOf<Reservation>()
                snapshot.documents.forEach { tableDoc ->
                    val tableId = tableDoc.id
                    firestore.collection("users")
                        .document(adminUserId)
                        .collection("places")
                        .document(placeId)
                        .collection("design")
                        .document(tableId)
                        .collection("reservations")
                        .document(date)
                        .collection("times")
                        .get()
                        .addOnSuccessListener { timeSnapshot ->
                            reservations.addAll(timeSnapshot.documents.mapNotNull { it.toObject(Reservation::class.java) })
                            onReservationsUpdated(reservations)
                        }
                }
            }
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error fetching reservations", e)
        }
    }



    override suspend fun getSavedReservationTimes(placeId: String): Flow<List<String>> = flow {
        val adminUserId = getAdminUserIdByPlace(placeId) ?: return@flow
        val document = firestore.collection("users")
            .document(adminUserId)
            .collection("places")
            .document(placeId)
            .get()
            .await()

        val times = document.get("reservationTimes") as? List<String> ?: emptyList()
        emit(times)
    }.catch {
        emit(emptyList())
    }

    override suspend fun getReservedTimesFromFirestore(
        placeId: String,
        tableId: String, date: String): List<String> {
        return try {
            val adminUserId = getAdminUserIdByPlace(placeId) ?: return emptyList()

            val snapshot = firestore.collection("users")
                .document(adminUserId)
                .collection("places")
                .document(placeId)
                .collection("design")
                .document(tableId)
                .collection("reservations")
                .document(date) // Fetch by date
                .collection("times")
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getString("time") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun markTableAsReserved(placeId: String, designId: String): Result<Unit> {
        return try {
            val adminUserId = getAdminUserIdByPlace(placeId)
                ?: return Result.failure(Exception("Admin User ID not found for this place"))

            firestore.collection("users")
                .document(adminUserId)
                .collection("places")
                .document(placeId)
                .collection("design")
                .document(designId)
                .update("reserved", true)
                .await()

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
                .await() //

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
