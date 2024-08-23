package com.tlc.domain.model.firebase

data class PlaceData(
    var name: String = "",
    var capacity: Int = 0,
    val id: String = "",
    val placeImageUrl: String
)