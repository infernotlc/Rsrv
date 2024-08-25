package com.tlc.domain.use_cases.design

import android.util.Log
import com.tlc.domain.repository.firebase.DesignRepository
import javax.inject.Inject

class LoadDesignUseCase @Inject constructor(
    private val designRepository: DesignRepository
) {
    suspend fun loadDesign(placeId: String)  =
        designRepository.getDesign(placeId)

    }
