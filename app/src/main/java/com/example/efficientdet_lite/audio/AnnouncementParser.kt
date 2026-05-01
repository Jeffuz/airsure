package com.example.efficientdet_lite.audio

import com.example.efficientdet_lite.announcements.Announcement
import com.example.efficientdet_lite.announcements.AnnouncementType
import com.example.efficientdet_lite.announcements.RiskLevel

class AnnouncementParser {
    fun parse(text: String): Announcement? {
        // This is a bridge/legacy parser to satisfy AudioViewModel
        return Announcement(
            id = System.currentTimeMillis().toString(),
            text = text,
            category = AnnouncementType.UNKNOWN,
            riskLevel = RiskLevel.LOW
        )
    }
}
