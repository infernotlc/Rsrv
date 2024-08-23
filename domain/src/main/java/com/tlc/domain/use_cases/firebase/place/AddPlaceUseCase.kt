package com.tlc.domain.use_cases.firebase.place

import com.tlc.domain.model.firebase.PlaceData
import com.tlc.domain.repository.firebase.PlaceRepository
import javax.inject.Inject

class AddPlaceUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(placeData: PlaceData) = placeRepository.addPlace(placeData)
}