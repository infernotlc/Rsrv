package com.tlc.domain.model.firebase

import com.google.firebase.Timestamp

data class Reservation(
    val chairId: String = "",
    val tableId: String = "",
    val customerId: String = "",
    val date: String = "",
    val time: String = "",
    val isApproved: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()
)