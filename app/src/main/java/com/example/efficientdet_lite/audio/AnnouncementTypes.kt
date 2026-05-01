package com.example.efficientdet_lite.audio

import java.time.LocalDateTime

data class Announcement(
    val id: String,
    val text: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val category: AnnouncementCategory = AnnouncementCategory.GENERAL,
    val riskLevel: RiskLevel = RiskLevel.LOW
)

enum class AnnouncementCategory {
    GATE_CHANGE,
    BOARDING,
    SECURITY,
    GENERAL,
    EMERGENCY
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
