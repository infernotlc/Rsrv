package com.tlc.data.ui.repository.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.PlaceData
import com.tlc.domain.model.firebase.Reservation
import com.tlc.domain.model.firebase.Place
import com.tlc.domain.repository.firebase.ReservationRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReservationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ReservationRepository {

    override suspend fun saveReservation(placeId: String, reservation: Reservation): Result<Unit> {
        return try {
            val adminUserId = getAdminUserIdByPlace(placeId)
                ?: return Result.failure(Exception("Admin User ID not found for this place"))

            val placeRef = firestore.collection("users")
                .document(adminUserId)
                .collection("places")
                .document(placeId)

            val placeDoc = placeRef.get().await()
            
            if (!placeDoc.exists()) {
                Log.e("ReservationRepository", "Place not found: $placeId")
                return Result.failure(Exception("Place not found"))
            }

            val place = placeDoc.toObject<Place>()
            if (place == null) {
                Log.e("ReservationRepository", "Failed to parse place document")
                return Result.failure(Exception("Invalid place data"))
            }

            // Create the reservation data
            val reservationData = hashMapOf(
                "id" to reservation.id,
                "userId" to reservation.userId,
                "placeId" to placeId,
                "placeName" to place.name,
                "tableId" to reservation.tableId,
                "holderName" to reservation.holderName,
                "holderPhoneNo" to reservation.holderPhoneNo,
                "customerCount" to reservation.customerCount,
                "animalCount" to reservation.animalCount,
                "date" to reservation.date,
                "time" to reservation.time,
                "status" to reservation.status,
                "cancellationNotes" to reservation.cancellationNotes,
                "timestamp" to reservation.timestamp
            )

            // Create a batch to perform multiple operations
            val batch = firestore.batch()

            // Create the date document first
            val dateDocRef = firestore.document("users/$adminUserId/places/$placeId/design/${reservation.tableId}/reservations/${reservation.date}")
            batch.set(dateDocRef, hashMapOf(
                "date" to reservation.date,
                "hasReservations" to true
            ))

            // Then create the reservation in times collection
            val timesDocRef = dateDocRef.collection("times").document(reservation.id)
            batch.set(timesDocRef, reservationData)

            // Save to customers collection (for faster customer access)
            val customerReservationRef = firestore
                .collection("customers")
                .document(reservation.userId)
                .collection("reservations")
                .document(reservation.id)
            batch.set(customerReservationRef, reservationData)

            // Commit all operations
            batch.commit().await()

            Log.d("ReservationRepository", "Reservation saved successfully with date document")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error saving reservation", e)
            Result.failure(e)
        }
    }

    override suspend fun getReservations(placeId: String, date: String): Flow<List<Reservation>> = callbackFlow {
        try {
            val adminUserId = getAdminUserIdByPlace(placeId) ?: throw Exception("Admin User ID not found for this place")
            
            val reservationRef = firestore.collection("users")
                .document(adminUserId)
                .collection("places")
                .document(placeId)
                .collection("design")

            val subscription = reservationRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

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
                            trySend(reservations)
                        }
                        .addOnFailureListener { e ->
                            close(e)
                        }
                }
            }

            awaitClose {
                subscription.remove()
            }
        } catch (e: Exception) {
            close(e)
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
        tableId: String,
        date: String
    ): List<String> {
        return try {
            val adminUserId = getAdminUserIdByPlace(placeId) ?: return emptyList()

            val snapshot = firestore.collection("users")
                .document(adminUserId)
                .collection("places")
                .document(placeId)
                .collection("design")
                .document(tableId)
                .collection("reservations")
                .document(date)
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
                .whereEqualTo("role", "admin")
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

    override suspend fun getUserReservations(userId: String): Flow<List<Reservation>> = callbackFlow {
        Log.d("ReservationRepository", "Starting to fetch reservations for user: $userId")
        
        try {
            val customerReservationsRef = firestore
                .collection("customers")
                .document(userId)
                .collection("reservations")

            val subscription = customerReservationsRef
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ReservationRepository", "Error fetching reservations", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.d("ReservationRepository", "No reservations found")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val reservations = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            Reservation(
                                id = data["id"] as? String ?: return@mapNotNull null,
                                userId = data["userId"] as? String ?: return@mapNotNull null,
                                placeId = data["placeId"] as? String ?: return@mapNotNull null,
                                placeName = data["placeName"] as? String ?: return@mapNotNull null,
                                tableId = data["tableId"] as? String ?: return@mapNotNull null,
                                holderName = data["holderName"] as? String ?: return@mapNotNull null,
                                holderPhoneNo = data["holderPhoneNo"] as? String ?: return@mapNotNull null,
                                customerCount = (data["customerCount"] as? Long)?.toInt() ?: return@mapNotNull null,
                                animalCount = (data["animalCount"] as? Long)?.toInt() ?: return@mapNotNull null,
                                date = data["date"] as? String ?: return@mapNotNull null,
                                time = data["time"] as? String ?: return@mapNotNull null,
                                status = data["status"] as? String ?: "active",
                                cancellationNotes = data["cancellationNotes"] as? String ?: "",
                                timestamp = data["timestamp"] as? com.google.firebase.Timestamp
                            )
                        } catch (e: Exception) {
                            Log.e("ReservationRepository", "Error parsing reservation document", e)
                            null
                        }
                    }

                    Log.d("ReservationRepository", "Found ${reservations.size} reservations for user: $userId")
                    trySend(reservations)
                }

            awaitClose {
                Log.d("ReservationRepository", "Closing reservations listener for user: $userId")
                subscription.remove()
            }
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error setting up reservations listener", e)
            close(e)
        }
    }


    override suspend fun fetchReservationsForPlace(placeId: String): List<Reservation> {
        val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        Log.d("ReservationRepository", "Fetching reservations for placeId: $placeId")

        return try {
            val reservations = mutableListOf<Reservation>()
            val snapshot = firestore.collection("users")
                .document(currentUserId)
                .collection("places")
                .document(placeId)
                .collection("design")
                .get()
                .await()

            for (tableDoc in snapshot.documents) {
                val timesSnapshot = firestore.collection("users")
                    .document(currentUserId)
                    .collection("places")
                    .document(placeId)
                    .collection("design")
                    .document(tableDoc.id)
                    .collection("reservations")
                    .get()
                    .await()

                for (dateDoc in timesSnapshot.documents) {
                    val reservationSnapshot = firestore.collection("users")
                        .document(currentUserId)
                        .collection("places")
                        .document(placeId)
                        .collection("design")
                        .document(tableDoc.id)
                        .collection("reservations")
                        .document(dateDoc.id)
                        .collection("times")
                        .get()
                        .await()

                    reservations.addAll(reservationSnapshot.documents.mapNotNull { it.toObject(Reservation::class.java) })
                }
            }

            reservations
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error fetching reservations: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getAdminPlaces(adminUserId: String): Flow<List<Place>> = callbackFlow {
        try {
            val placesRef = firestore.collection("users")
                .document(adminUserId)
                .collection("places")

            val subscription = placesRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val places = snapshot.documents.mapNotNull { it.toObject(Place::class.java) }
                trySend(places)
            }

            awaitClose {
                subscription.remove()
            }
        } catch (e: Exception) {
            close(e)
        }
    }

    override suspend fun getAllAdminReservations(adminUserId: String): Flow<List<Reservation>> = callbackFlow {
        try {
            Log.d("ReservationRepository", "Starting to fetch all reservations for admin: $adminUserId")
            
            val placesRef = firestore.collection("users")
                .document(adminUserId)
                .collection("places")

            val subscription = placesRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ReservationRepository", "Error fetching places", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    Log.d("ReservationRepository", "No places found for admin")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val allReservations = mutableListOf<Reservation>()
                    
                    for (placeDoc in snapshot.documents) {
                        try {
                            val placeId = placeDoc.id
                            Log.d("ReservationRepository", "Processing place: $placeId")
                            
                            val designRef = placeDoc.reference.collection("design")
                            val designSnapshot = designRef.get().await()
                            
                            for (tableDoc in designSnapshot.documents) {
                                val tableId = tableDoc.id
                                Log.d("ReservationRepository", "Processing table: $tableId")
                                
                                val reservationsRef = tableDoc.reference.collection("reservations")
                                val datesSnapshot = reservationsRef.get().await()
                                
                                for (dateDoc in datesSnapshot.documents) {
                                    val date = dateDoc.id
                                    Log.d("ReservationRepository", "Processing date: $date")
                                    
                                    val timesRef = dateDoc.reference.collection("times")
                                    val timeSnapshot = timesRef.get().await()
                                    
                                    Log.d("ReservationRepository", "Found ${timeSnapshot.documents.size} reservations for date: $date")
                                    
                                    for (reservationDoc in timeSnapshot.documents) {
                                        try {
                                            val data = reservationDoc.data ?: continue
                                            Log.d("ReservationRepository", "Found reservation data: $data")
                                            
                                            val reservation = Reservation(
                                                id = data["id"] as? String ?: continue,
                                                userId = data["userId"] as? String ?: continue,
                                                placeId = data["placeId"] as? String ?: continue,
                                                placeName = data["placeName"] as? String ?: continue,
                                                tableId = data["tableId"] as? String ?: continue,
                                                holderName = data["holderName"] as? String ?: continue,
                                                holderPhoneNo = data["holderPhoneNo"] as? String ?: continue,
                                                customerCount = (data["customerCount"] as? Long)?.toInt() ?: continue,
                                                animalCount = (data["animalCount"] as? Long)?.toInt() ?: 0,
                                                date = data["date"] as? String ?: continue,
                                                time = data["time"] as? String ?: continue,
                                                status = data["status"] as? String ?: "active",
                                                cancellationNotes = data["cancellationNotes"] as? String ?: "",
                                                timestamp = data["timestamp"] as? com.google.firebase.Timestamp
                                            )
                                            allReservations.add(reservation)
                                            Log.d("ReservationRepository", "Added reservation: ${reservation.id}")
                                        } catch (e: Exception) {
                                            Log.e("ReservationRepository", "Error parsing reservation document", e)
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ReservationRepository", "Error fetching reservations for place ${placeDoc.id}", e)
                        }
                    }
                    
                    Log.d("ReservationRepository", "Total reservations found: ${allReservations.size}")
                    trySend(allReservations)
                }
            }

            awaitClose {
                subscription.remove()
            }
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error in getAllAdminReservations", e)
            close(e)
        }
    }

    override suspend fun cancelReservation(reservationId: String, userId: String, cancellationNotes: String): Result<Unit> {
        return try {
            Log.d("ReservationRepository", "Cancelling reservation: $reservationId for user: $userId with notes: $cancellationNotes")
            
            // First, get the reservation details from customer collection
            val customerReservationRef = firestore
                .collection("customers")
                .document(userId)
                .collection("reservations")
                .document(reservationId)
            
            val reservationDoc = customerReservationRef.get().await()
            if (!reservationDoc.exists()) {
                return Result.failure(Exception("Reservation not found"))
            }
            
            val reservationData = reservationDoc.data ?: return Result.failure(Exception("Invalid reservation data"))
            val placeId = reservationData["placeId"] as? String ?: return Result.failure(Exception("Place ID not found"))
            val tableId = reservationData["tableId"] as? String ?: return Result.failure(Exception("Table ID not found"))
            val date = reservationData["date"] as? String ?: return Result.failure(Exception("Date not found"))
            
            // Get admin user ID for this place
            val adminUserId = getAdminUserIdByPlace(placeId) ?: return Result.failure(Exception("Admin User ID not found for this place"))
            
            // Create a batch to perform multiple operations
            val batch = firestore.batch()
            
            // Update status and cancellation notes in customer collection
            batch.update(customerReservationRef, 
                mapOf(
                    "status" to "cancelled",
                    "cancellationNotes" to cancellationNotes
                )
            )
            
            // Update status and cancellation notes in admin collection
            val adminReservationRef = firestore
                .collection("users")
                .document(adminUserId)
                .collection("places")
                .document(placeId)
                .collection("design")
                .document(tableId)
                .collection("reservations")
                .document(date)
                .collection("times")
                .document(reservationId)
            
            batch.update(adminReservationRef, 
                mapOf(
                    "status" to "cancelled",
                    "cancellationNotes" to cancellationNotes
                )
            )
            
            // Commit all operations
            batch.commit().await()
            
            Log.d("ReservationRepository", "Reservation cancelled successfully with notes: $cancellationNotes")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ReservationRepository", "Error cancelling reservation", e)
            Result.failure(e)
        }
    }
}
