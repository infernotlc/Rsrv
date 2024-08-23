package com.tlc.data.remote.dto.firebase_dto

import com.tlc.domain.model.firebase.Table
import java.io.Serializable
import java.util.UUID

data class PlaceDataDto(
    val id: String = UUID.randomUUID().toString(),
    val name: String="",
    val placeImageUrl: String="",
    val capacity: Int=0,
): Serializable