package com.example.efficientdet_lite

import com.example.efficientdet_lite.audio.AnnouncementCategory
import com.example.efficientdet_lite.audio.AnnouncementParser
import com.example.efficientdet_lite.audio.RiskLevel
import com.example.efficientdet_lite.rules.TravelRisk
import com.example.efficientdet_lite.rules.TravelRule
import com.example.efficientdet_lite.rules.TravelRuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioLogicTest {

    private val parser = AnnouncementParser()
    private val ruleEngine = TravelRuleEngine()

    @Test
    fun `test gate change announcement parsing`() {
        val text = "Attention, gate for flight 123 has changed to B42"
        val announcement = parser.parse(text)
        
        assertEquals(AnnouncementCategory.GATE_CHANGE, announcement.category)
        assertEquals(RiskLevel.MEDIUM, announcement.riskLevel)
    }

    @Test
    fun `test security alert parsing`() {
        val text = "Security alert: Please keep your belongings with you"
        val announcement = parser.parse(text)
        
        assertEquals(AnnouncementCategory.SECURITY, announcement.category)
        assertEquals(RiskLevel.HIGH, announcement.riskLevel)
    }

    @Test
    fun `test rule engine evaluation`() {
        val text = "Gate change for flight 456"
        val announcement = parser.parse(text)
        val rules = ruleEngine.evaluateAnnouncement(announcement)
        
        assertTrue(rules.any { it is TravelRule.AnnouncementRule && it.risk == TravelRisk.MINOR_DELAY })
    }
}
