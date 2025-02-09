package com.tlc.domain.model.firebase

import com.google.firebase.firestore.PropertyName
import java.util.UUID

data class DesignItem(
    val designId: String = UUID.randomUUID().toString(),
    val type: String = "",
    var xPosition: Float = 0f,
    var yPosition: Float = 0f,

    @get:PropertyName("reserved") @set:PropertyName("reserved")
    var isReserved: Boolean = false,
)
