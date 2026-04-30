package com.example.efficientdet_lite.audio

object DemoAnnouncements {
    val list = listOf(
        Announcement(
            id = "1",
            text = "Attention all passengers, gate for flight AF123 has been changed to B24.",
            category = AnnouncementCategory.GATE_CHANGE,
            riskLevel = RiskLevel.MEDIUM
        ),
        Announcement(
            id = "2",
            text = "Final boarding call for flight DL456 to Atlanta at Gate A12.",
            category = AnnouncementCategory.BOARDING,
            riskLevel = RiskLevel.LOW
        ),
        Announcement(
            id = "3",
            text = "Security alert: Please do not leave your bags unattended.",
            category = AnnouncementCategory.SECURITY,
            riskLevel = RiskLevel.HIGH
        )
    )
}
