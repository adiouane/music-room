package com.example.musicroom.presentation.events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    // TODO: Inject EventsApiService when you create it
) : ViewModel() {
    
    private val _publicEventsState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val publicEventsState: StateFlow<EventsUiState> = _publicEventsState.asStateFlow()
    
    private val _myEventsState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val myEventsState: StateFlow<EventsUiState> = _myEventsState.asStateFlow()
    
    private val _selectedTab = MutableStateFlow(EventTab.PUBLIC)
    val selectedTab: StateFlow<EventTab> = _selectedTab.asStateFlow()
    
    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()
    
    private val _createResult = MutableStateFlow<CreateEventResult?>(null)
    val createResult: StateFlow<CreateEventResult?> = _createResult.asStateFlow()
    
    init {
        Log.d("EventsViewModel", "🚀 ViewModel initialized, loading mock events")
        loadMockData()
    }
    
    fun switchTab(tab: EventTab) {
        Log.d("EventsViewModel", "🔄 Switching to tab: ${tab.displayName}")
        _selectedTab.value = tab
    }
    
    fun refreshCurrentTab() {
        Log.d("EventsViewModel", "🔄 Refreshing current tab")
        loadMockData()
    }
    
    fun createEvent(
        title: String,
        location: String,
        description: String?,
        isPublic: Boolean,
        startTime: String
    ) {
        viewModelScope.launch {
            try {
                _isCreating.value = true
                Log.d("EventsViewModel", "🆕 Creating event: $title")
                
                // TODO: Replace with real API call
                kotlinx.coroutines.delay(1000) // Simulate API call
                
                _createResult.value = CreateEventResult.Success("Event '$title' created successfully!")
                loadMockData() // Refresh the list
                
            } catch (e: Exception) {
                Log.e("EventsViewModel", "❌ Failed to create event", e)
                _createResult.value = CreateEventResult.Error("Failed to create event: ${e.message}")
            } finally {
                _isCreating.value = false
            }
        }
    }
    
    fun clearCreateResult() {
        _createResult.value = null
    }
    
    // TODO: Replace with real API calls
    private fun loadMockData() {
        viewModelScope.launch {
            try {
                _publicEventsState.value = EventsUiState.Loading
                _myEventsState.value = EventsUiState.Loading
                
                // Simulate API delay
                kotlinx.coroutines.delay(500)
                
                // Mock data - replace with real API calls later
                val mockEvents = emptyList<Event>() // Empty for now
                
                _publicEventsState.value = EventsUiState.Success(mockEvents)
                _myEventsState.value = EventsUiState.Success(mockEvents)
                
            } catch (e: Exception) {
                Log.e("EventsViewModel", "❌ Failed to load events", e)
                _publicEventsState.value = EventsUiState.Error("Failed to load events")
                _myEventsState.value = EventsUiState.Error("Failed to load events")
            }
        }
    }
}