package com.tlc.domain.model.firebase

import java.io.Serializable

data class PlaceData(
    var name: String = "",
    var capacity: Int = 0,
    var id: String = "",
    var placeImageUrl: String
):Serializable