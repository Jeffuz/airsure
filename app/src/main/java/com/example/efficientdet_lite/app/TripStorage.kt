package com.example.efficientdet_lite.app

import android.content.Context

object TripStorage {
    private const val PREFS_NAME = "airsure_trip_prefs"

    private const val KEY_FROM = "from"
    private const val KEY_TO = "to"
    private const val KEY_DATE = "date"
    private const val KEY_FLIGHT = "flight"
    private const val KEY_GATE = "gate"

    fun load(context: Context): TripDetails {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        return TripDetails(
            from = prefs.getString(KEY_FROM, "") ?: "",
            to = prefs.getString(KEY_TO, "") ?: "",
            date = prefs.getString(KEY_DATE, "") ?: "",
            flight = prefs.getString(KEY_FLIGHT, "") ?: "",
            gate = prefs.getString(KEY_GATE, "") ?: ""
        )
    }

    fun save(context: Context, tripDetails: TripDetails) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_FROM, tripDetails.from)
            .putString(KEY_TO, tripDetails.to)
            .putString(KEY_DATE, tripDetails.date)
            .putString(KEY_FLIGHT, tripDetails.flight)
            .putString(KEY_GATE, tripDetails.gate)
            .apply()
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
