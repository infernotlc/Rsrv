package com.tlc.domain.use_cases.design

import com.tlc.domain.model.firebase.DesignItem
import com.tlc.domain.repository.firebase.DesignRepository
import javax.inject.Inject

class SaveDesignUseCase @Inject constructor(
    private val designRepository: DesignRepository
){
    suspend fun saveDesign(placeId: String, designItems: List<DesignItem>) =
        designRepository.saveDesign(placeId, designItems)
}