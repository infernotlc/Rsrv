package com.tlc.data.mapper.firebase_mapper

import com.tlc.domain.model.firebase.Place
import com.tlc.data.remote.dto.firebase_dto.PlaceDataDto
import com.tlc.domain.model.firebase.PlaceData

fun PlaceDataDto.toPlaceData(): PlaceData {
    return PlaceData(
        name = name,
        capacity = capacity,
        id = id,
        placeImageUrl = placeImageUrl,
        reservationTimes = reservationTimes,
        country = country,
        city = city
    )
}

fun PlaceData.toPlaceDataDto(): PlaceDataDto {
    return PlaceDataDto(
        name = name,
        capacity = capacity,
        id = id,
        placeImageUrl = placeImageUrl,
        reservationTimes = reservationTimes,
        country = country,
        city = city
    )
}

fun PlaceData.toPlace(): Place {
    return Place(
        placeImageUrl = placeImageUrl,
        id = id,
        name = name,
        capacity = capacity,
        reservationTimes = reservationTimes
    )
}