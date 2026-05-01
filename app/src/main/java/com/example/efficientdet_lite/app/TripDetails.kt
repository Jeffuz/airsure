package com.example.efficientdet_lite.app

data class TripDetails(
    val from: String = "",
    val to: String = "",
    val date: String = "",
    val flight: String = "",
    val gate: String = ""
) {
    fun hasAnyValue(): Boolean {
        return from.isNotBlank() ||
                to.isNotBlank() ||
                date.isNotBlank() ||
                flight.isNotBlank() ||
                gate.isNotBlank()
    }
}
