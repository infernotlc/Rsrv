package com.tlc.domain.repository.firebase

import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow

interface DesignRepository {
    suspend fun saveDesign(placeId: String, designItems: List<DesignItem>): Flow<RootResult<Unit>>
    suspend fun getDesign(placeId: String): Flow<RootResult<List<DesignItem>>>
}