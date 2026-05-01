package com.example.efficientdet_lite.announcements

object AnnouncementParser {

    /**
     * Cleans up Whisper's "spaced out" transcription style.
     * Converts "A A A 1 2 3" -> "AAA123"
     */
    fun normalize(text: String): String {
        val upper = text.uppercase()
        
        // 1. Handle spaced out characters: "A B 1 2" -> "AB12"
        // We look for single characters separated by spaces and join them.
        val joinedText = upper.replace(Regex("(?<=\\b[A-Z0-9])\\s(?=[A-Z0-9]\\b)"), "")
        
        // 2. Standard cleanup: Remove punctuation and extra spaces
        return joinedText
            .replace(Regex("[^A-Z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun extractFlightNumbers(text: String): List<String> {
        val normalized = normalize(text)

        // Patterns that are now much more flexible with spaces
        val patterns = listOf(
            Regex("AA\\s?\\d+"),
            Regex("UA\\s?\\d+"),
            Regex("DL\\s?\\d+"),
            Regex("WN\\s?\\d+"),
            Regex("AMERICAN\\s?AIRLINES?\\s?FLIGHT\\s?(\\d+)"),
            Regex("UNITED\\s?AIRLINES?\\s?FLIGHT\\s?(\\d+)"),
            Regex("DELTA\\s?AIRLINES?\\s?FLIGHT\\s?(\\d+)"),
            Regex("FLIGHT\\s?(\\d+)")
        )

        val matches = mutableListOf<String>()

        // Check for common airline keywords first
        if (normalized.contains("AMERICAN") || normalized.contains("AA")) {
            val num = Regex("\\d+").find(normalized.substringAfter("AMERICAN").substringAfter("AA"))?.value
            if (num != null) matches.add("AA$num")
        }
        if (normalized.contains("UNITED") || normalized.contains("UA")) {
            val num = Regex("\\d+").find(normalized.substringAfter("UNITED").substringAfter("UA"))?.value
            if (num != null) matches.add("UA$num")
        }

        // Generic fallback
        patterns.forEach { pattern ->
            pattern.find(normalized)?.let { match ->
                val canonical = canonicalizeFlightNumber(match.value)
                if (canonical != null) matches.add(canonical)
            }
        }

        return matches.distinct()
    }

    fun canonicalizeFlightNumber(raw: String): String? {
        val text = raw.replace(" ", "")
        
        val airlineCode = when {
            text.contains("AA") || text.contains("AMERICAN") -> "AA"
            text.contains("UA") || text.contains("UNITED") -> "UA"
            text.contains("DL") || text.contains("DELTA") -> "DL"
            text.contains("WN") || text.contains("SOUTHWEST") -> "WN"
            else -> "AA" // Default for demo if flight number is found
        }

        val number = Regex("\\d+").find(text)?.value

        return if (number != null) {
            "$airlineCode$number"
        } else {
            null
        }
    }

    fun extractGate(text: String): String? {
        val normalized = normalize(text)
        // Match "GATE 24B" or just "GATE B" or "GATE 12"
        val gatePattern = Regex("GATE\\s?([A-Z]?\\d{1,3}[A-Z]?)")
        return gatePattern.find(normalized)?.groupValues?.getOrNull(1)
    }

    fun classify(text: String): AnnouncementType {
        val normalized = normalize(text)

        return when {
            normalized.contains("GATE CHANGE") || (normalized.contains("GATE") && normalized.contains("CHANGE")) ->
                AnnouncementType.GATE_CHANGE

            normalized.contains("BOARDING") ->
                AnnouncementType.BOARDING

            normalized.contains("FINAL CALL") || normalized.contains("LAST CALL") ->
                AnnouncementType.FINAL_CALL

            normalized.contains("DELAY") ->
                AnnouncementType.DELAY

            else -> AnnouncementType.UNKNOWN
        }
    }
}
