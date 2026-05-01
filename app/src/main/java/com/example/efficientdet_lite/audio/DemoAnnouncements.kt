package com.example.efficientdet_lite.audio

import com.example.efficientdet_lite.announcements.Announcement
import com.example.efficientdet_lite.announcements.AnnouncementType
import com.example.efficientdet_lite.announcements.RiskLevel

object DemoAnnouncements {
    val list = listOf(
        Announcement(
            id = "1",
            text = "Attention all passengers, gate for flight AF123 has been changed to B24.",
            category = AnnouncementType.GATE_CHANGE,
            riskLevel = RiskLevel.MEDIUM
        ),
        Announcement(
            id = "2",
            text = "Final boarding call for flight DL456 to Atlanta at Gate A12.",
            category = AnnouncementType.BOARDING,
            riskLevel = RiskLevel.LOW
        ),
        Announcement(
            id = "3",
            text = "Security alert: Please do not leave your bags unattended.",
            category = AnnouncementType.SECURITY,
            riskLevel = RiskLevel.HIGH
        )
    )
}
