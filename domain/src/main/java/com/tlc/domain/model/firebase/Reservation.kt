package com.tlc.domain.model.firebase

import com.google.firebase.Timestamp

import com.google.firebase.firestore.PropertyName

data class Reservation(
    val tableId: String = "",
    val holderName: String = "",
    val holderPhoneNo: String = "",
    val customerCount: Int = 0,
    val animalCount: Int = 0,
    val date: String = "",
    val time: String = "",
// or use @field:JvmField  instead of those ones
    @get:PropertyName("reserved") @set:PropertyName("reserved")
    var isReserved: Boolean = false,

    @get:PropertyName("approved") @set:PropertyName("approved")
    var isApproved: Boolean = false,

    val timestamp: Timestamp? = null
)
