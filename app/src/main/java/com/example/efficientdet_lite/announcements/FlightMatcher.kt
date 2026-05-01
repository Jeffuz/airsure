package com.example.efficientdet_lite.announcements

object FlightMatcher {

    fun isMatch(
        transcriptFlightNumbers: List<String>,
        userFlight: UserFlight
    ): Boolean {
        val userCanonical = AnnouncementParser.canonicalizeFlightNumber(userFlight.flightNumber)
            ?: return false

        // Be lenient: if user is AA123 and we hear AA1232 (because of "to"), it's a match.
        return transcriptFlightNumbers.any { found -> 
            found == userCanonical || found.contains(userCanonical) 
        }
    }
}
