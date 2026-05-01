package com.example.efficientdet_lite.announcements

object AnnouncementParser {

    fun normalize(text: String): String {
        return text
            .uppercase()
            .replace(Regex("[^A-Z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun extractFlightNumbers(text: String): List<String> {
        val normalized = normalize(text)

        val patterns = listOf(
            Regex("\\bAA\\s?\\d{1,4}\\b"),
            Regex("\\bUA\\s?\\d{1,4}\\b"),
            Regex("\\bDL\\s?\\d{1,4}\\b"),
            Regex("\\bWN\\s?\\d{1,4}\\b"),
            Regex("\\bSWA\\s?\\d{1,4}\\b"),
            Regex("\\bAMERICAN\\s+(AIRLINES\\s+)?(FLIGHT\\s+)?\\d{1,4}\\b"),
            Regex("\\bUNITED\\s+(AIRLINES\\s+)?(FLIGHT\\s+)?\\d{1,4}\\b"),
            Regex("\\bDELTA\\s+(AIRLINES\\s+)?(FLIGHT\\s+)?\\d{1,4}\\b")
        )

        val matches = mutableListOf<String>()

        for (pattern in patterns) {
            pattern.findAll(normalized).forEach { match ->
                val value = match.value
                val canonical = canonicalizeFlightNumber(value)
                if (canonical != null) {
                    matches.add(canonical)
                }
            }
        }

        return matches.distinct()
    }

    fun canonicalizeFlightNumber(raw: String): String? {
        val text = normalize(raw)

        val airlineCode = when {
            text.startsWith("AA") || text.startsWith("AMERICAN") -> "AA"
            text.startsWith("UA") || text.startsWith("UNITED") -> "UA"
            text.startsWith("DL") || text.startsWith("DELTA") -> "DL"
            text.startsWith("WN") || text.startsWith("SWA") || text.startsWith("SOUTHWEST") -> "WN"
            else -> null
        }

        val number = Regex("\\d{1,4}").find(text)?.value

        return if (airlineCode != null && number != null) {
            "$airlineCode$number"
        } else {
            null
        }
    }

    fun extractGate(text: String): String? {
        val normalized = normalize(text)

        val gatePattern = Regex("\\bGATE\\s+([A-Z]?\\d{1,3}[A-Z]?)\\b")
        return gatePattern.find(normalized)?.groupValues?.getOrNull(1)
    }

    fun classify(text: String): AnnouncementType {
        val normalized = normalize(text)

        return when {
            normalized.contains("GATE") &&
                    (normalized.contains("CHANGE") || normalized.contains("CHANGED") || normalized.contains("NOW DEPARTS")) ->
                AnnouncementType.GATE_CHANGE

            normalized.contains("BOARDING") || normalized.contains("NOW BOARDING") ->
                AnnouncementType.BOARDING

            normalized.contains("FINAL CALL") || normalized.contains("LAST CALL") ->
                AnnouncementType.FINAL_CALL

            normalized.contains("DELAY") || normalized.contains("DELAYED") ->
                AnnouncementType.DELAY

            else -> AnnouncementType.UNKNOWN
        }
    }
}
