package com.tlc.domain.model.firebase

data class Reservation(
    val chairId: String = "",
    val tableId: String = "",
    val customerId: String = "",
    val date: String = "",
    val time: String = "",
    val isApproved: Boolean = false
)
