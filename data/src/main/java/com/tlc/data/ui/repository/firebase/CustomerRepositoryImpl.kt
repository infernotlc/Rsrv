package com.tlc.data.ui.repository.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.domain.model.firebase.Place
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.repository.firebase.CustomerRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class CustomerRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CustomerRepository {

    override suspend fun getCustomerPlaces(): Flow<RootResult<List<Place>>> = flow {
        emit(RootResult.Loading)
        try {
            val placesList = mutableListOf<Place>()
            val usersSnapshot = firestore.collection("users")
                .whereEqualTo("role", "admin")
                .get()
                .await()

            for (userDoc in usersSnapshot.documents) {
                val placesSnapshot = userDoc.reference.collection("places").get().await()
                for (placeDoc in placesSnapshot.documents) {
                    val place = placeDoc.toObject(Place::class.java)
                    place?.let { placesList.add(it) }
                }
            }
            emit(RootResult.Success(placesList))
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Failed to load places"))
        }
    }

    override suspend fun getCustomerDesign(placeId: String): Flow<RootResult<List<DesignItem>>> =
        flow {
            emit(RootResult.Loading)
            Log.d("getCustomerDesign", "Fetching design for placeId: $placeId")
            try {
                val designItems = mutableListOf<DesignItem>()

                val usersSnapshot = firestore.collection("users")
                    .whereEqualTo("role", "admin") // Assuming admin users have the designs
                    .get()
                    .await()

                Log.d("getCustomerDesign", "Admin users found: ${usersSnapshot.size()}")

                for (userDoc in usersSnapshot.documents) {
                    val placesSnapshot = firestore.collection("users")
                        .document(userDoc.id)
                        .collection("places")
                        .whereEqualTo("id", placeId)
                        .get()
                        .await()

                    Log.d(
                        "getCustomerDesign",
                        "Places found for user ${userDoc.id}: ${placesSnapshot.size()}"
                    )

                    for (placeDoc in placesSnapshot.documents) {
                        val designSnapshot = firestore.collection("users")
                            .document(userDoc.id)
                            .collection("places")
                            .document(placeDoc.id)
                            .collection("design")
                            .get()
                            .await()

                        Log.d(
                            "getCustomerDesign",
                            "Design documents found: ${designSnapshot.size()}"
                        )

                        for (designDoc in designSnapshot.documents) {
                            val designItem = designDoc.toObject(DesignItem::class.java)
                            designItem?.let {
                                designItems.add(it)
                                Log.d("getCustomerDesign", "Design item added: $it")
                            }
                        }
                    }
                }

                if (designItems.isEmpty()) {
                    Log.d("getCustomerDesign", "No design items found.")
                }

                emit(RootResult.Success(designItems))
            } catch (e: Exception) {
                Log.e("getCustomerDesign", "Error fetching design: ${e.message}", e)
                emit(RootResult.Error(e.message ?: "Failed to load design"))
            }
        }
}

