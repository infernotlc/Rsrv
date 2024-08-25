package com.tlc.domain.model.firebase

import java.util.UUID

data class DesignItem(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "",
    var xPosition: Float = 0f,
    var yPosition: Float = 0f
)
