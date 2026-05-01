package com.example.efficientdet_lite.announcements

enum class AnnouncementType {
    GATE_CHANGE,
    BOARDING,
    DELAY,
    FINAL_CALL,
    SECURITY,
    UNKNOWN
}

// Re-add legacy name for backward compatibility if needed, 
// but it's better to update the callers.
typealias AnnouncementCategory = AnnouncementType

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

data class Announcement(
    val id: String,
    val text: String,
    val category: AnnouncementType,
    val riskLevel: RiskLevel
)

data class UserFlight(
    val flightNumber: String, // "AA123"
    val from: String? = null,
    val to: String? = null,
    val gate: String? = null
)

data class FlightAnnouncement(
    val matchedFlightNumber: String,
    val type: AnnouncementType,
    val gate: String? = null,
    val delayText: String? = null,
    val rawTranscript: String
)
