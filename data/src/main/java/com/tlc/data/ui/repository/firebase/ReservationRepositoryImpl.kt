package com.tlc.data.ui.repository.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.domain.utils.RootResult
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

            Log.d("ReservationRepository", "Saving reservations for placeId: $placeId, adminUserId: $adminUserId")

            // Get place name
            val placeDoc = firestore.collection("users")
                .document(adminUserId)
                .collection("places")
                .document(placeId)
                .get()
                .await()

            val placeName = placeDoc.getString("name") ?: "Unknown Place"
            Log.d("ReservationRepository", "Found place name: $placeName")

            // Group reservations by date and table
            val groupedReservations = reservations.groupBy { "${it.tableId}_${it.date}" }

            groupedReservations.forEach { (key, dateReservations) ->
                val (tableId, date) = key.split("_")
                
                // Create the date document first
                val dateRef = firestore.collection("users")
                    .document(adminUserId)
                    .collection("places")
                    .document(placeId)
                    .collection("design")
                    .document(tableId)
                    .collection("reservations")
                    .document(date)

                // Save date document with metadata
                val dateData = hashMapOf(
                    "date" to date,
                    "tableId" to tableId,
                    "createdAt" to System.currentTimeMillis(),
                    "hasReservations" to true
                )
                dateRef.set(dateData).await()
                Log.d("ReservationRepository", "Created date document for: $date with data: $dateData")

                // Now save all reservations for this date
                dateReservations.forEach { reservation ->
                    val timeRef = dateRef.collection("times").document()

                    Log.d("ReservationRepository", "Saving reservation with path: users/$adminUserId/places/$placeId/design/$tableId/reservations/$date/times/${timeRef.id}")

                    // Add place name, userId, placeId, and id to reservation
                    val reservationWithPlace = reservation.copy(
                        id = timeRef.id,
                        placeId = placeId,
                        placeName = placeName,
                        userId = auth.currentUser?.uid ?: "",
                        tableId = tableId,
                        date = date
                    )

                    Log.d("ReservationRepository", "Saving reservation data: $reservationWithPlace")
                    timeRef.set(reservationWithPlace).await()
                    
                    // Verify the reservation was saved
                    val savedDoc = timeRef.get().await()
                    Log.d("ReservationRepository", "Verified saved reservation: ${savedDoc.data}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error saving reservation: ${e.message}", e)
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

    override suspend fun getUserReservations(userId: String): RootResult<List<Reservation>> {
        return try {
            val reservations = mutableListOf<Reservation>()
            
            Log.d("ReservationRepository", "Starting getUserReservations for userId: $userId")
            
            val adminUsers = firestore.collection("users")
                .whereEqualTo("role", "admin")
                .get()
                .await()

            Log.d("ReservationRepository", "Found ${adminUsers.size()} admin users")

            for (adminDoc in adminUsers.documents) {
                val adminUserId = adminDoc.id
                Log.d("ReservationRepository", "Checking admin user: $adminUserId")
                
                val placesSnapshot = firestore.collection("users")
                    .document(adminUserId)
                    .collection("places")
                    .get()
                    .await()

                Log.d("ReservationRepository", "Found ${placesSnapshot.size()} places for admin $adminUserId")

                for (placeDoc in placesSnapshot.documents) {
                    val placeId = placeDoc.id
                    val placeName = placeDoc.getString("name") ?: "Unknown Place"
                    Log.d("ReservationRepository", "Checking place: $placeName (ID: $placeId)")
                    
                    val designSnapshot = firestore.collection("users")
                        .document(adminUserId)
                        .collection("places")
                        .document(placeId)
                        .collection("design")
                        .get()
                        .await()

                    Log.d("ReservationRepository", "Found ${designSnapshot.size()} design items for place $placeId")

                    for (designDoc in designSnapshot.documents) {
                        val tableId = designDoc.id
                        Log.d("ReservationRepository", "Checking table: $tableId")
                        Log.d("FirestoreDebug", "adminUserId: $adminUserId, placeId: $placeId, tableId: $tableId")
                        val reservationsSnapshot = firestore.collection("users")
                            .document(adminUserId)
                            .collection("places")
                            .document(placeId)
                            .collection("design")
                            .document(tableId)
                            .collection("reservations")
                            .get()
                            .await()

                        Log.d("FirestoreDebug", "Reservations found: ${reservationsSnapshot.documents.size}")
                        Log.d("ReservationRepository", "Found ${reservationsSnapshot.size()} reservation dates for table $tableId")
                        
                        reservationsSnapshot.documents.forEach { doc ->
                            Log.d("ReservationRepository", "Reservation date document ID: ${doc.id}")
                        }

                        for (dateDoc in reservationsSnapshot.documents) {
                            val date = dateDoc.id
                            Log.d("ReservationRepository", "Checking date: $date")
                            
                            val timesSnapshot = firestore.collection("users")
                                .document(adminUserId)
                                .collection("places")
                                .document(placeId)
                                .collection("design")
                                .document(tableId)
                                .collection("reservations")
                                .document(date)
                                .collection("times")
                                .whereEqualTo("userId", userId)
                                .get()
                                .await()

                            Log.d("ReservationRepository", "Found ${timesSnapshot.size()} reservations for date $date with userId $userId")

                            timesSnapshot.documents.forEach { doc ->
                                Log.d("ReservationRepository", "Reservation document data: ${doc.data}")
                                val reservation = doc.toObject(Reservation::class.java)
                                Log.d("ReservationRepository", "Parsed reservation: $reservation")
                                
                                if (reservation != null) {
                                    val updatedReservation = reservation.copy(
                                        id = doc.id,
                                        placeId = placeId,
                                        placeName = placeName,
                                        tableId = tableId,
                                        date = date
                                    )
                                    Log.d("ReservationRepository", "Updated reservation: $updatedReservation")
                                    reservations.add(updatedReservation)
                                } else {
                                    Log.e("ReservationRepository", "Failed to parse reservation from document: ${doc.data}")
                                }
                            }
                        }
                    }
                }
            }

            Log.d("ReservationRepository", "Total reservations found: ${reservations.size}")
            reservations.forEach { reservation ->
                Log.d("ReservationRepository", "Final reservation: $reservation")
            }
            
            RootResult.Success(reservations)
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error fetching user reservations: ${e.message}", e)
            RootResult.Error(e.message ?: "Failed to load reservations")
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
