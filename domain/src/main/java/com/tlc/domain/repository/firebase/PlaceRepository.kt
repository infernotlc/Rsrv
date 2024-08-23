package com.tlc.domain.repository.firebase

import android.net.Uri
import com.tlc.domain.model.firebase.PlaceData
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow

interface PlaceRepository {
    suspend fun addPlace(placeData: PlaceData): Flow<RootResult<Boolean>>
    suspend fun deletePlace(competitionId: String): Flow<RootResult<Boolean>>
    suspend fun updatePlace(placeId: String, placeData: PlaceData): Flow<RootResult<Boolean>>
    suspend fun getAllPlaces(): Flow<RootResult<List<PlaceData>>>
    suspend fun uploadImage(uri: Uri, imagePathString: String): Flow<RootResult<String>>

}