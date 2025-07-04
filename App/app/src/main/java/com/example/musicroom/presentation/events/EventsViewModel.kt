package com.example.musicroom.presentation.events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.Event
import com.example.musicroom.data.service.EventsApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsApiService: EventsApiService
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
        Log.d("EventsViewModel", "🚀 ViewModel initialized, loading events from backend")
        loadAllEvents()
    }
    
    fun switchTab(tab: EventTab) {
        Log.d("EventsViewModel", "🔄 Switching to tab: ${tab.displayName}")
        _selectedTab.value = tab
        
        // Load data if not already loaded
        when (tab) {
            EventTab.PUBLIC -> {
                if (_publicEventsState.value is EventsUiState.Loading) {
                    loadPublicEvents()
                }
            }
            EventTab.MY_EVENTS -> {
                if (_myEventsState.value is EventsUiState.Loading) {
                    loadMyEvents()
                }
            }
        }
    }
    
    fun refreshCurrentTab() {
        Log.d("EventsViewModel", "🔄 Refreshing current tab")
        when (_selectedTab.value) {
            EventTab.PUBLIC -> loadPublicEvents()
            EventTab.MY_EVENTS -> loadMyEvents()
        }
    }
    
    fun loadAllEvents() {
        Log.d("EventsViewModel", "🔄 Loading all events (public + my events)")
        loadPublicEvents()
        loadMyEvents()
    }
    
    fun loadPublicEvents() {
        viewModelScope.launch {
            try {
                Log.d("EventsViewModel", "📋 Loading public events from backend")
                _publicEventsState.value = EventsUiState.Loading
                
                val result = eventsApiService.getPublicEvents()
                
                if (result.isSuccess) {
                    val events = result.getOrThrow()
                    Log.d("EventsViewModel", "✅ Successfully loaded ${events.size} public events")
                    
                    events.forEachIndexed { index, event ->
                        Log.d("EventsViewModel", "🎪 Public event $index: ${event.title} at ${event.location}")
                    }
                    
                    _publicEventsState.value = EventsUiState.Success(events)
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("EventsViewModel", "❌ Failed to load public events", error)
                    _publicEventsState.value = EventsUiState.Error(error?.message ?: "Failed to load public events")
                }
                
            } catch (e: Exception) {
                Log.e("EventsViewModel", "❌ Exception loading public events", e)
                _publicEventsState.value = EventsUiState.Error("Error loading public events: ${e.message}")
            }
        }
    }
    
    fun loadMyEvents() {
        viewModelScope.launch {
            try {
                Log.d("EventsViewModel", "📋 Loading my events from backend")
                _myEventsState.value = EventsUiState.Loading
                
                val result = eventsApiService.getMyEvents()
                
                if (result.isSuccess) {
                    val events = result.getOrThrow()
                    Log.d("EventsViewModel", "✅ Successfully loaded ${events.size} my events")
                    
                    events.forEachIndexed { index, event ->
                        Log.d("EventsViewModel", "🎪 My event $index: ${event.title} at ${event.location}")
                    }
                    
                    _myEventsState.value = EventsUiState.Success(events)
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("EventsViewModel", "❌ Failed to load my events", error)
                    _myEventsState.value = EventsUiState.Error(error?.message ?: "Failed to load my events")
                }
                
            } catch (e: Exception) {
                Log.e("EventsViewModel", "❌ Exception loading my events", e)
                _myEventsState.value = EventsUiState.Error("Error loading my events: ${e.message}")
            }
        }
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
                Log.d("EventsViewModel", "🆕 Creating event: $title at $location")
                
                // For now, use a simple datetime format. You can improve this later with proper date/time picker
                val formattedStartTime = if (startTime.isNotBlank()) {
                    // Assume format like "2024-07-15 20:00" and convert to ISO format
                    if (startTime.contains("T")) {
                        startTime // Already in ISO format
                    } else {
                        "$startTime:00Z" // Convert to ISO format
                    }
                } else {
                    // Default to current time + 1 hour using SimpleDateFormat (API 21+)
                    val currentTime = System.currentTimeMillis() + (60 * 60 * 1000) // 1 hour from now
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    sdf.format(Date(currentTime))
                }
                
                val result = eventsApiService.createEvent(
                    title = title,
                    description = description,
                    location = location,
                    eventStartTime = formattedStartTime,
                    eventEndTime = null, // Could add end time field later
                    isPublic = isPublic
                )
                
                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    Log.d("EventsViewModel", "✅ Event created successfully: ${response.eventId}")
                    _createResult.value = CreateEventResult.Success(response.message)
                    
                    // Refresh the events list
                    loadAllEvents()
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("EventsViewModel", "❌ Failed to create event", error)
                    _createResult.value = CreateEventResult.Error(error?.message ?: "Failed to create event")
                }
                
            } catch (e: Exception) {
                Log.e("EventsViewModel", "❌ Exception creating event", e)
                _createResult.value = CreateEventResult.Error("Error creating event: ${e.message}")
            } finally {
                _isCreating.value = false
            }
        }
    }
    
    fun clearCreateResult() {
        _createResult.value = null
    }
}