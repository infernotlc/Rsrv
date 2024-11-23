package com.tlc.domain.model.firebase

data class Reservation(
    val reservationId: String = "", // Optional: Firestore auto-generated ID
    val customerId: String = "",
    val date: String = "", // Format: "dd-MM-yyyy"
    val timeSlot: String = "", // Example: "10:00-12:00"
    val tables: List<String> = emptyList(), // List of table IDs
    val chairs: List<String> = emptyList(), // List of chair IDs
    val status: ReservationStatus = ReservationStatus.CONFIRMED,
    val items: List<DesignItem> = emptyList()
)

enum class ReservationStatus {
    CONFIRMED,
    CANCELLED,
    NO_SHOW
}
