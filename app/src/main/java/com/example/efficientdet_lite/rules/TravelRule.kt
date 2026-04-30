package com.example.efficientdet_lite.rules

import com.example.efficientdet_lite.audio.AnnouncementCategory

sealed class TravelRule {
    abstract val name: String
    abstract val description: String
    abstract val risk: TravelRisk

    data class ProhibitedItemRule(
        override val name: String,
        val label: String,
        override val description: String,
        override val risk: TravelRisk = TravelRisk.PROHIBITED_ITEM
    ) : TravelRule()

    data class AnnouncementRule(
        override val name: String,
        val category: AnnouncementCategory,
        override val description: String,
        override val risk: TravelRisk
    ) : TravelRule()
}
