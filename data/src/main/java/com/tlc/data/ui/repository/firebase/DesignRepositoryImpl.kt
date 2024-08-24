package com.tlc.data.ui.repository.firebase

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
                firestore.collection("users")
                    .document(userId)
                    .collection("places")
                    .document(placeId)
                    .collection("design")
                    .apply {
                        // Add a batch write to save all design items
                        val batch = firestore.batch()
                        designItems.forEach { item ->
                            val itemRef = this.document(item.id)
                            batch.set(itemRef, item)
                        }
                        batch.commit().await()
                    }
                emit(RootResult.Success(Unit))
            } else {
                emit(RootResult.Error("User ID is null"))
            }
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getDesign(placeId: String): Flow<RootResult<List<DesignItem>>> = flow {
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
                emit(RootResult.Success(designItems))
            } else {
                emit(RootResult.Error("User ID is null"))
            }
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)
}
