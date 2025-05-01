package com.tlc.data.ui.repository.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.tlc.data.mapper.firebase_mapper.toPlaceData
import com.tlc.data.mapper.firebase_mapper.toPlaceDataDto
import com.tlc.data.remote.dto.firebase_dto.PlaceDataDto
import com.tlc.domain.model.firebase.PlaceData
import com.tlc.domain.repository.firebase.PlaceRepository
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class PlaceRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PlaceRepository {

    override suspend fun addPlace(placeData: PlaceData): Flow<RootResult<Boolean>> = flow {
        emit(RootResult.Loading)
        try {
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            if (userId != null) {
                val placeId = firestore.collection("users").document(userId).collection("places")
                    .document().id

                val placeInfo = placeData.toPlaceDataDto().copy(
                    id = placeId,
                    reservationTimes = placeData.reservationTimes.toList(),
                    placeImageUrl = placeData.placeImageUrl,
                    country = placeData.country,
                    city = placeData.city
                )

                Log.d("FirestoreSave", "Saving place: $placeInfo")

                firestore.collection("users").document(userId).collection("places")
                    .document(placeId).set(placeInfo).await()

                emit(RootResult.Success(true))
            } else {
                emit(RootResult.Error("User ID is null"))
            }
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deletePlace(competitionId: String): Flow<RootResult<Boolean>> =
        flow {
            emit(RootResult.Loading)
            try {
                val currentUser = firebaseAuth.currentUser
                val userId = currentUser?.uid
                if (userId != null) {
                    val placeRef = firestore.collection("users")
                        .document(userId)
                        .collection("places")
                        .document(competitionId)

                    val designCollectionRef = placeRef.collection("design")

                    val designDocs = designCollectionRef.get().await()
                    designDocs.documents.forEach { document ->
                        document.reference.delete().await()
                    }

                    placeRef.delete().await()

                    emit(RootResult.Success(true))
                } else {
                    emit(RootResult.Error("User ID is null"))
                }
            } catch (e: Exception) {
                emit(RootResult.Error(e.message ?: "Something went wrong"))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun updatePlace(
        placeId: String,
        placeData: PlaceData
    ): Flow<RootResult<Boolean>> = flow {
        emit(RootResult.Loading)
        try {
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            if (userId != null) {
                val placeInfo = placeData.toPlaceDataDto().copy(
                    id = placeId,
                    reservationTimes = placeData.reservationTimes.toList()
                )
                val placeRef =
                    firestore.collection("users").document(userId).collection("places")
                        .document(placeId)
                placeRef.set(placeInfo).await()
                emit(RootResult.Success(true))
            } else {
                emit(RootResult.Error("User ID is null"))
            }
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getAllPlaces(): Flow<RootResult<List<PlaceData>>> = flow {
        emit(RootResult.Loading)
        try {
            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            if (userId != null) {
                val querySnapshot =
                    firestore.collection("users").document(userId).collection("places").get()
                        .await()
                val placeList = querySnapshot.documents.mapNotNull { document ->
                    val placeDataDto = document.toObject(PlaceDataDto::class.java)
                    placeDataDto?.toPlaceData()
                }
                emit(RootResult.Success(placeList))
            } else {
                emit(RootResult.Error("User ID is null"))
            }
        } catch (e: Exception) {
            emit(RootResult.Error(e.message ?: "Something went wrong"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uploadImage(uri: Uri, imagePathString: String): Flow<RootResult<String>> =
        flow {
            emit(RootResult.Loading)
            try {
                val storageRef = storage.reference.child("$imagePathString/${UUID.randomUUID()}.jpg")
                val uploadTask = storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                emit(RootResult.Success(downloadUrl.toString()))
            } catch (e: Exception) {
                emit(RootResult.Error(e.message ?: "Image upload failed"))
            }
        }.flowOn(Dispatchers.IO)

}