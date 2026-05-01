package com.example.efficientdet_lite.announcements

object AnnouncementProcessor {

    fun process(
        transcript: String,
        userFlight: UserFlight
    ): FlightAnnouncement? {
        if (transcript.isBlank()) return null

        val flightNumbers = AnnouncementParser.extractFlightNumbers(transcript)

        if (!FlightMatcher.isMatch(flightNumbers, userFlight)) {
            return null
        }

        val type = AnnouncementParser.classify(transcript)

        if (type == AnnouncementType.UNKNOWN) {
            return null
        }

        val gate = AnnouncementParser.extractGate(transcript)
        val matchedFlight = AnnouncementParser.canonicalizeFlightNumber(userFlight.flightNumber)
            ?: userFlight.flightNumber

        return FlightAnnouncement(
            matchedFlightNumber = matchedFlight,
            type = type,
            gate = gate,
            delayText = if (type == AnnouncementType.DELAY) transcript else null,
            rawTranscript = transcript
        )
    }
}
