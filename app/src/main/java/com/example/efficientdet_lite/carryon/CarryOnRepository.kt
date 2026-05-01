package com.example.efficientdet_lite.carryon

import com.qualcomm.qti.objectdetection.RestrictionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CarryOnRepository {
    private val _savedItems = MutableStateFlow<List<Pair<String, RestrictionManager.TravelInfo?>>>(emptyList())
    val savedItems: StateFlow<List<Pair<String, RestrictionManager.TravelInfo?>>> = _savedItems.asStateFlow()

    fun addItems(newItems: List<Pair<String, RestrictionManager.TravelInfo?>>) {
        // Simple in-memory storage for now, avoiding duplicates of the same item label for clarity
        val current = _savedItems.value.toMutableList()
        newItems.forEach { newItem ->
            if (current.none { it.first == newItem.first }) {
                current.add(newItem)
            }
        }
        _savedItems.value = current
    }

    fun clear() {
        _savedItems.value = emptyList()
    }
}