package com.example.efficientdet_lite.rules

import com.example.efficientdet_lite.audio.Announcement
import com.example.efficientdet_lite.audio.AnnouncementCategory

class TravelRuleEngine {
    private val rules = listOf(
        TravelRule.ProhibitedItemRule("Scissors", "scissors", "Scissors are not allowed in carry-on."),
        TravelRule.AnnouncementRule("Gate Change", AnnouncementCategory.GATE_CHANGE, "Your gate has changed. Check the new location.", TravelRisk.MINOR_DELAY),
        TravelRule.AnnouncementRule("Security Alert", AnnouncementCategory.SECURITY, "Security alert in your area.", TravelRisk.SECURITY_VIOLATION)
    )

    fun evaluateAnnouncement(announcement: Announcement): List<TravelRule> {
        return rules.filter { it is TravelRule.AnnouncementRule && it.category == announcement.category }
    }
}
