package com.example.efficientdet_lite.audio

import java.util.UUID

class AnnouncementParser {
    
    fun parse(text: String): Announcement {
        val category = when {
            text.contains("gate", ignoreCase = true) -> AnnouncementCategory.GATE_CHANGE
            text.contains("boarding", ignoreCase = true) -> AnnouncementCategory.BOARDING
            text.contains("security", ignoreCase = true) -> AnnouncementCategory.SECURITY
            text.contains("emergency", ignoreCase = true) -> AnnouncementCategory.EMERGENCY
            else -> AnnouncementCategory.GENERAL
        }

        val riskLevel = when (category) {
            AnnouncementCategory.EMERGENCY -> RiskLevel.CRITICAL
            AnnouncementCategory.SECURITY -> RiskLevel.HIGH
            AnnouncementCategory.GATE_CHANGE -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return Announcement(
            id = UUID.randomUUID().toString(),
            text = text,
            category = category,
            riskLevel = riskLevel
        )
    }
}
