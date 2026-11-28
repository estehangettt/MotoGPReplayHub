package com.estehangettt.motogp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.estehangettt.motogp.data.EventResponse
import com.estehangettt.motogp.data.api.MotoGPApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val api = MotoGPApi()
    
    private val _events = MutableStateFlow<List<EventResponse>>(emptyList())
    val events: StateFlow<List<EventResponse>> = _events

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // State Kategori (motogp, moto2, moto3)
    private val _selectedCategory = MutableStateFlow("motogp")
    val selectedCategory: StateFlow<String> = _selectedCategory

    // State Event yang lagi dipilih di Hero Banner
    private val _selectedEvent = MutableStateFlow<EventResponse?>(null)
    val selectedEvent: StateFlow<EventResponse?> = _selectedEvent

    init {
        fetchEvents()
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setEvent(event: EventResponse) {
        _selectedEvent.value = event
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            val rawEvents = api.getEvents(2026)
            // Urutin berdasarkan tanggal
            val sorted = rawEvents.sortedBy { it.date_start }
            _events.value = sorted
            // Set event pertama sebagai default
            if (sorted.isNotEmpty()) {
                _selectedEvent.value = sorted.first()
            }
            _isLoading.value = false
        }
    }
}
