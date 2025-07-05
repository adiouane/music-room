package com.example.musicroom.presentation.events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.Event
import com.example.musicroom.data.service.EventsApiService
import com.example.musicroom.data.service.CreateEventRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * UI States for different data types
 */
sealed class EventsUiState {
    object Loading : EventsUiState()
    data class Success(val events: List<Event>) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}

sealed class CreateEventResult {
    data class Success(val eventId: String, val title: String) : CreateEventResult()
    data class Error(val message: String) : CreateEventResult()
}

enum class EventTab(val displayName: String) {
    PUBLIC("Public Events"),
    MY_EVENTS("My Events")
}

/**
 * ViewModel for Events screen with separate states
 * Similar to PlaylistDetailsViewModel structure
 */
@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsApiService: EventsApiService
) : ViewModel() {
    
    // Separate states for each tab
    private val _publicEventsState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val publicEventsState: StateFlow<EventsUiState> = _publicEventsState.asStateFlow()
    
    private val _myEventsState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val myEventsState: StateFlow<EventsUiState> = _myEventsState.asStateFlow()
    
    // Tab management
    private val _selectedTab = MutableStateFlow(EventTab.PUBLIC)
    val selectedTab: StateFlow<EventTab> = _selectedTab.asStateFlow()
    
    // Create event state
    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()
    
    private val _createResult = MutableStateFlow<CreateEventResult?>(null)
    val createResult: StateFlow<CreateEventResult?> = _createResult.asStateFlow()
    
    init {
        loadPublicEvents()
        loadMyEvents()
    }
    
    /**
     * Load public events
     */
    private fun loadPublicEvents() {
        viewModelScope.launch {
            try {
                _publicEventsState.value = EventsUiState.Loading
                Log.d("EventsVM", "📋 Loading public events...")
                
                val result = eventsApiService.getPublicEvents()
                if (result.isSuccess) {
                    val events = result.getOrThrow()
                    Log.d("EventsVM", "✅ Loaded ${events.size} public events")
                    _publicEventsState.value = EventsUiState.Success(events)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("EventsVM", "❌ Failed to load public events: $error")
                    _publicEventsState.value = EventsUiState.Error("Failed to load public events: $error")
                }
                
            } catch (e: Exception) {
                Log.e("EventsVM", "❌ Error loading public events", e)
                _publicEventsState.value = EventsUiState.Error("Failed to load public events: ${e.message}")
            }
        }
    }
    
    /**
     * Load user's events
     */
    private fun loadMyEvents() {
        viewModelScope.launch {
            try {
                _myEventsState.value = EventsUiState.Loading
                Log.d("EventsVM", "📋 Loading my events...")
                
                val result = eventsApiService.getMyEvents()
                if (result.isSuccess) {
                    val events = result.getOrThrow()
                    Log.d("EventsVM", "✅ Loaded ${events.size} my events")
                    _myEventsState.value = EventsUiState.Success(events)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("EventsVM", "❌ Failed to load my events: $error")
                    _myEventsState.value = EventsUiState.Error("Failed to load my events: $error")
                }
                
            } catch (e: Exception) {
                Log.e("EventsVM", "❌ Error loading my events", e)
                _myEventsState.value = EventsUiState.Error("Failed to load my events: ${e.message}")
            }
        }
    }
    
    /**
     * Switch between tabs
     */
    fun switchTab(tab: EventTab) {
        _selectedTab.value = tab
    }
    
    /**
     * Refresh current tab
     */
    fun refreshCurrentTab() {
        when (_selectedTab.value) {
            EventTab.PUBLIC -> loadPublicEvents()
            EventTab.MY_EVENTS -> loadMyEvents()
        }
    }
    
    /**
     * Refresh all data
     */
    fun refresh() {
        loadPublicEvents()
        loadMyEvents()
    }
    
    /**
     * Create a new event
     */
    fun createEvent(
        title: String,
        description: String?,
        location: String,
        startTime: String?,
        isPublic: Boolean
    ) {
        viewModelScope.launch {
            try {
                _isCreating.value = true
                Log.d("EventsVM", "🎪 Creating event: $title")
                
                // Format start time properly
                val formattedStartTime = if (!startTime.isNullOrBlank()) {
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
                
                val request = CreateEventRequest(
                    title = title,
                    description = description,
                    location = location,
                    event_start_time = formattedStartTime,
                    event_end_time = null, // Could add end time field later
                    is_public = isPublic
                )
                
                val result = eventsApiService.createEvent(request)
                
                if (result.isSuccess) {
                    val response = result.getOrThrow()
                    Log.d("EventsVM", "✅ Event created successfully: ${response.eventId}")
                    _createResult.value = CreateEventResult.Success(
                        eventId = response.eventId ?: "",
                        title = response.title ?: title
                    )
                    
                    // Refresh both tabs to show the new event
                    loadPublicEvents()
                    loadMyEvents()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("EventsVM", "❌ Failed to create event: $error")
                    _createResult.value = CreateEventResult.Error(error)
                }
                
            } catch (e: Exception) {
                Log.e("EventsVM", "❌ Error creating event", e)
                _createResult.value = CreateEventResult.Error("Failed to create event: ${e.message}")
            } finally {
                _isCreating.value = false
            }
        }
    }
    
    /**
     * Clear create result
     */
    fun clearCreateResult() {
        _createResult.value = null
    }
}