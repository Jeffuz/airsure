package com.example.efficientdet_lite.announcements
import com.example.efficientdet_lite.app.TripDetails
object AnnouncementProcessor {

    fun process(
        transcript: String,
        tripDetails: TripDetails
    ): FlightAnnouncement? {
        if (transcript.isBlank()) return null

        // 1. Create a "Dense" version of the transcript (all caps, no spaces, no punctuation)
        // "American Airlines Flight A A 1 2 3 2" -> "AMERICANAIRLINESFLIGHTAA1232"
        val denseTranscript = transcript.uppercase().replace(Regex("[^A-Z0-9]"), "")

        // 2. Create dense versions of our target
        val targetFlight = tripDetails.flight.uppercase().replace(Regex("[^A-Z0-9]"), "")
        val targetDigits = targetFlight.filter { it.isDigit() }

        // 3. Build a list of things that would count as a match for this user
        val matchVariants = mutableListOf(targetFlight, targetDigits)

        // Add full-name variants for common airlines
        when {
            targetFlight.startsWith("AA") -> matchVariants.add("AMERICAN" + targetDigits)
            targetFlight.startsWith("UA") -> matchVariants.add("UNITED" + targetDigits)
            targetFlight.startsWith("DL") -> matchVariants.add("DELTA" + targetDigits)
            targetFlight.startsWith("WN") -> matchVariants.add("SOUTHWEST" + targetDigits)
        }

        // 4. Matcher: Does the dense transcript contain ANY of our target variants?
        // This handles "AA1232" matching "AA123" perfectly.
        val isMatch = matchVariants.any { denseTranscript.contains(it) }

        if (!isMatch) return null

        // 5. If it's a match, use the Parser to extract the secondary details (Gate, Type)
        val type = AnnouncementParser.classify(transcript)
        val gate = AnnouncementParser.extractGate(transcript)

        // We only care if it's a known announcement type for the MVP
        if (type == AnnouncementType.UNKNOWN) return null

        return FlightAnnouncement(
            matchedFlightNumber = targetFlight,
            type = type,
            gate = gate,
            delayText = if (type == AnnouncementType.DELAY) transcript else null,
            rawTranscript = transcript
        )
    }
}
