package com.tlc.data.mapper.firebase_mapper

import com.tlc.data.remote.dto.firebase_dto.PlaceDataDto
import com.tlc.domain.model.firebase.PlaceData

fun PlaceDataDto.toPlaceData(): PlaceData {
    return PlaceData(
        placeImageUrl = placeImageUrl,
        id = id,
        name = name,
        capacity = capacity,
    )
}

fun PlaceData.toPlaceDataDto(): PlaceDataDto {
    return PlaceDataDto(
        placeImageUrl = placeImageUrl,
        id = id,
        name = name,
        capacity = capacity,
    )
}