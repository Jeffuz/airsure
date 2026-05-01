package com.example.efficientdet_lite.app

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TripRepository {
    private val _tripDetails = MutableStateFlow(TripDetails())
    val tripDetails: StateFlow<TripDetails> = _tripDetails.asStateFlow()

    fun init(context: Context) {
        _tripDetails.value = TripStorage.load(context)
    }

    fun updateTrip(context: Context, newDetails: TripDetails) {
        _tripDetails.value = newDetails
        TripStorage.save(context, newDetails)
    }

    fun updateGate(context: Context, newGate: String) {
        if (newGate.isBlank() || _tripDetails.value.gate == newGate) return
        val updated = _tripDetails.value.copy(gate = newGate)
        _tripDetails.value = updated
        TripStorage.save(context, updated)
    }
}
