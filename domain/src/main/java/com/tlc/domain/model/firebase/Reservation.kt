package com.tlc.domain.model.firebase

import com.google.firebase.Timestamp

data class Reservation(
    val tableId: String = "",
    val holderName: String = "",
    val holderPhoneNo: String = "",
    val customerCount: Int = 0,
    val animalCount: Int = 0,
    val date: String = "",
    val time: String = "",
    val isReserved: Boolean = true,
    val isApproved: Boolean = false,
    val timestamp: Timestamp = Timestamp.now()

)