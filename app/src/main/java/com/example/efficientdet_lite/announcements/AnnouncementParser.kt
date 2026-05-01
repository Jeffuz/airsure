package com.example.efficientdet_lite.announcements

object AnnouncementParser {

    fun normalize(text: String): String {
        return text.uppercase()
            .replace(Regex("[^A-Z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // This is still used by the processor to find the gate number
    fun extractGate(text: String): String? {
        val normalized = normalize(text)
        val gatePattern = Regex("GATE\\s?([A-Z]?\\d{1,3}[A-Z]?)")
        return gatePattern.find(normalized)?.groupValues?.getOrNull(1)
    }

    fun classify(text: String): AnnouncementType {
        val normalized = normalize(text)

        return when {
            normalized.contains("GATE CHANGE") || (normalized.contains("GATE") && normalized.contains("CHANGE")) ->
                AnnouncementType.GATE_CHANGE

            normalized.contains("BOARDING") || normalized.contains("BOARD") ->
                AnnouncementType.BOARDING

            normalized.contains("FINAL CALL") || normalized.contains("LAST CALL") ->
                AnnouncementType.FINAL_CALL

            normalized.contains("DELAY") || normalized.contains("LATE") ->
                AnnouncementType.DELAY

            else -> AnnouncementType.UNKNOWN
        }
    }
    
    // Legacy support for older files
    fun extractFlightNumbers(text: String): List<String> = emptyList()
    fun canonicalizeFlightNumber(raw: String): String? = raw
}
