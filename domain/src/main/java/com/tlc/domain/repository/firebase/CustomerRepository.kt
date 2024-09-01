package com.tlc.domain.repository.firebase

import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.model.firebase.Place
import com.tlc.domain.utils.RootResult
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun getCustomerPlaces(): Flow<RootResult<List<Place>>>
    suspend fun getCustomerDesign(placeId: String): Flow<RootResult<List<DesignItem>>>
}