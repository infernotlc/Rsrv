package com.tlc.data.ui.repository.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.repository.firebase.DesignRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DesignRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : DesignRepository {

    override suspend fun saveDesign(placeId: String, designItems: List<DesignItem>): Flow<RootResult<Unit>> = flow {
        emit(RootResult.Loading)
        try {
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            if (userId != null) {
                val batch = firestore.batch()
                designItems.forEach { item ->
                    val itemRef = firestore.collection("users")
                        .document(userId)
                        .collection("places")
                        .document(placeId)
                        .collection("design")
                        .document(item.designId)
                    batch.set(itemRef, item)
                }
                batch.commit().await()
                emit(RootResult.Success(Unit))
            } else {
                emit(RootResult.Error("User ID is null"))
            }
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun getDesign(placeId: String): Flow<RootResult<List<DesignItem>>> = flow {
            Log.d("DesignRepositoryImpl", "getDesign called with placeId: $placeId")
            emit(RootResult.Loading)
            try {
                val currentUser = firebaseAuth.currentUser
                val userId = currentUser?.uid
                if (userId != null) {
                    val querySnapshot =
                        firestore.collection("users").document(userId)
                            .collection("places").document(placeId)
                            .collection("design").get().await()
                    val designItems = querySnapshot.documents.mapNotNull { document ->
                        document.toObject(DesignItem::class.java)
                    }
                    Log.d("DesignRepositoryImpl", "Design items fetched: $designItems")
                    emit(RootResult.Success(designItems))
                } else {
                    emit(RootResult.Error("User ID is null"))
                }
            } catch (e: Exception) {
                Log.e("DesignRepositoryImpl", "Error fetching design: ${e.message}")
                emit(RootResult.Error(e.message ?: "Something went wrong"))
            }
        }.flowOn(Dispatchers.IO)
    }