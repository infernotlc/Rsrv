package com.tlc.domain.model.firebase

import com.google.firebase.Timestamp
import java.util.UUID

data class DesignItem(
    val designId: String = UUID.randomUUID().toString(),
    val type: String = "",
    var xPosition: Float = 0f,
    var yPosition: Float = 0f,
    var isReserved: Boolean = false,
    var reservedBy: String? = "",
    var reservationStartTime: Timestamp?= null,
    var reservationEndTime: Timestamp? = null
)
