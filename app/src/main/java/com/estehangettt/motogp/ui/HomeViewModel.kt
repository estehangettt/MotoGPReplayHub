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

    init {
        fetchEvents()
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _events.value = api.getEvents(2026)
            _isLoading.value = false
        }
    }
}
