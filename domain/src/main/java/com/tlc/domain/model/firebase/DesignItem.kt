package com.tlc.domain.model.firebase

import java.util.UUID

data class DesignItem(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    var xPosition: Float,
    var yPosition: Float
)