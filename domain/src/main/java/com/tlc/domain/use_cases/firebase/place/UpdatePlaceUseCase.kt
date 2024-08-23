package com.tlc.domain.use_cases.firebase.place

import com.tlc.domain.model.firebase.PlaceData
import com.tlc.domain.repository.firebase.PlaceRepository
import javax.inject.Inject

class UpdatePlaceUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
)
{
    suspend operator fun invoke(placeId: String, placeData: PlaceData) = placeRepository.updatePlace(placeId, placeData)

}