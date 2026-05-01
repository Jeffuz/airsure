package com.example.efficientdet_lite.announcements

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AnnouncementRepository {
    private val _activeAlert = MutableStateFlow<FlightAnnouncement?>(null)
    val activeAlert: StateFlow<FlightAnnouncement?> = _activeAlert.asStateFlow()

    fun setAlert(alert: FlightAnnouncement?) {
        _activeAlert.value = alert
    }

    fun clear() {
        _activeAlert.value = null
    }
}
