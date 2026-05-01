package com.example.efficientdet_lite.announcements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class FlightViewModel : ViewModel() {
    
    // User's boarding pass info
    var userFlight by mutableStateOf<UserFlight?>(null)
        private set

    // Detected alerts for this flight
    var activeAlert by mutableStateOf<FlightAnnouncement?>(null)
        private set

    fun updateFlightInfo(flight: UserFlight) {
        userFlight = flight
    }

    fun setAlert(alert: FlightAnnouncement) {
        activeAlert = alert

        // Update the boarding pass gate immediately if a gate is found in the announcement
        // (Applies to GATE_CHANGE, BOARDING, etc.)
        alert.gate?.let { newGate ->
            userFlight?.let { currentFlight ->
                if (currentFlight.gate != newGate) {
                    updateFlightInfo(currentFlight.copy(gate = newGate))
                }
            }
        }
    }

    fun clearAlert() {
        activeAlert = null
    }
}
