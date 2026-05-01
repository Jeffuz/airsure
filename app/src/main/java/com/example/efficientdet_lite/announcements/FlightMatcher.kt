package com.example.efficientdet_lite.announcements

object FlightMatcher {

    fun isMatch(
        transcriptFlightNumbers: List<String>,
        userFlight: UserFlight
    ): Boolean {
        val userCanonical = AnnouncementParser.canonicalizeFlightNumber(userFlight.flightNumber)
            ?: return false

        return transcriptFlightNumbers.any { it == userCanonical }
    }
}
