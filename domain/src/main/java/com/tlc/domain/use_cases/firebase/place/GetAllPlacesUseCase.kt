package com.tlc.domain.use_cases.firebase.place

import com.tlc.domain.repository.firebase.PlaceRepository
import javax.inject.Inject

class GetAllPlacesUseCase @Inject constructor(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke() = placeRepository.getAllPlaces()
}