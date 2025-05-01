package com.tlc.data.remote.dto.firebase_dto


import java.util.UUID

data class PlaceDataDto(
    val id: String = UUID.randomUUID().toString(),
    val name: String="",
    val placeImageUrl: String="",
    val capacity: Int=0,
    val reservationTimes: List<String> = emptyList(),
    val country: String = "",
    val city: String = ""
)