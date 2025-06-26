/**
 * ========================================================================================
 * MUSIC SEARCH VIEW MODEL
 * ========================================================================================
 * 
 * Handles music search functionality with mock data implementation.
 * Ready for backend integration when music API is available.
 * 
 * 🎵 CURRENT FUNCTIONALITY:
 * ========================================================================================
 * ✅ Mock music search with realistic data
 * ✅ Loading states and error handling
 * ✅ State management with Kotlin Flow
 * ✅ Coroutine-based async operations
 * 
 * 🔄 FOR DEVELOPERS - BACKEND INTEGRATION:
 * ========================================================================================
 * 1. Replace generateMockTracks() with real API service
 * 2. Inject music API service (Spotify, Apple Music, etc.)
 * 3. Update searchTracks() to call real endpoints
 * 4. Handle API responses and error cases
 * 5. Add pagination if needed for large result sets
 * 
 * 🧪 MOCK DATA BEHAVIOR:
 * ========================================================================================
 * - Simulates 1-second network delay
 * - Returns 3 tracks per search query
 * - Track names include search query for relevance
 * - Uses consistent mock data structure
 * 
 * 📊 STATE MANAGEMENT:
 * ========================================================================================
 * - Empty: Initial state before any search
 * - Loading: During API call (with mock delay)
 * - Success: Search completed with results
 * - Error: Search failed or no results found
 * ========================================================================================
 */
package com.example.musicroom.presentation.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class MusicSearchViewModel @Inject constructor() : ViewModel() {

    // ============================================================================
    // STATE MANAGEMENT - UI state flow for reactive programming
    // ============================================================================
    private val _uiState = MutableStateFlow<MusicSearchUiState>(MusicSearchUiState.Empty)
    val uiState: StateFlow<MusicSearchUiState> = _uiState.asStateFlow()

    /**
     * ========================================================================
     * SEARCH TRACKS FUNCTION
     * ========================================================================
     * 
     * Performs music search with mock data. Ready for backend integration.
     * 
     * @param query Search term entered by user
     * 
     * 🔄 FLOW:
     * 1. Validate query (not blank)
     * 2. Set loading state
     * 3. Simulate network delay (1 second)
     * 4. Generate mock results based on query
     * 5. Update state with success or error
     * 
     * 💡 BACKEND INTEGRATION NOTES:
     * - Replace generateMockTracks() with real API call
     * - Add error handling for network failures
     * - Implement proper search result mapping
     * - Consider adding debouncing for search-as-you-type
     * ========================================================================
     */
    fun searchTracks(query: String) {
        if (query.isBlank()) return // Don't search empty queries
        
        viewModelScope.launch {
            _uiState.value = MusicSearchUiState.Loading
            
            try {
                // 🧪 MOCK IMPLEMENTATION - Replace with real API call
                delay(1000) // Simulate network delay
                
                val mockTracks = generateMockTracks(query)
                
                _uiState.value = if (mockTracks.isEmpty()) {
                    MusicSearchUiState.Error("No tracks found for '$query'")
                } else {
                    MusicSearchUiState.Success(mockTracks)
                }
            } catch (e: Exception) {
                // Handle any errors during search
                _uiState.value = MusicSearchUiState.Error("Search failed: ${e.message}")
            }
        }
    }
    
    /**
     * ========================================================================
     * MOCK DATA GENERATOR
     * ========================================================================
     * 
     * Generates realistic mock music data for testing and development.
     * 
     * @param query Search term to incorporate into track names
     * @return List of mock tracks with realistic data
     * 
     * 🎵 MOCK DATA STRUCTURE:
     * - Track ID: "mock_{number}_{query}" format
     * - Title: Includes search query for relevance
     * - Artist: Variety of mock artists (A, B, C)
     * - Duration: Realistic song lengths (3-4 minutes)
     * - Thumbnail: Empty for now (add placeholder URLs if needed)
     * 
     * 🔄 FOR BACKEND INTEGRATION:
     * Replace this entire function with real API response mapping:
     * - Parse API response JSON
     * - Map to Track data class
     * - Handle missing fields gracefully
     * - Validate data integrity
     * ========================================================================
     */
    private fun generateMockTracks(query: String): List<Track> {
        // Generate some mock tracks based on query
        return listOf(            Track(
                id = "mock_1_$query",
                title = "$query - Song 1",
                artist = "Artist A",
                thumbnailUrl = "", // TODO: Add placeholder image URLs when needed
                duration = "3:45"
            ),
            Track(
                id = "mock_2_$query",
                title = "$query - Song 2", 
                artist = "Artist B",
                thumbnailUrl = "", // TODO: Add placeholder image URLs when needed
                duration = "4:12"
            ),
            Track(
                id = "mock_3_$query",
                title = "$query - Song 3",
                artist = "Artist C", 
                thumbnailUrl = "", // TODO: Add placeholder image URLs when needed
                duration = "3:28"
            )
        )
    }
}

/**
 * ============================================================================
 * MUSIC SEARCH UI STATE
 * ============================================================================
 * 
 * Sealed class representing all possible states of the music search feature.
 * Enables type-safe state management and reactive UI updates.
 * 
 * 🎯 STATE DEFINITIONS:
 * ============================================================================
 * Empty   : Initial state, no search performed yet
 * Loading : Search in progress, show loading indicator
 * Success : Search completed with results, display track list
 * Error   : Search failed or no results, show error message
 * 
 * 📱 UI INTEGRATION:
 * ============================================================================
 * Use with collectAsState() in Composables to react to state changes:
 * 
 * when (uiState) {
 *     is MusicSearchUiState.Empty -> ShowSearchPrompt()
 *     is MusicSearchUiState.Loading -> ShowLoadingSpinner()
 *     is MusicSearchUiState.Success -> ShowTrackList(uiState.tracks)
 *     is MusicSearchUiState.Error -> ShowErrorMessage(uiState.message)
 * }
 * ============================================================================
 */
sealed class MusicSearchUiState {
    object Empty : MusicSearchUiState()                      // No search yet
    object Loading : MusicSearchUiState()                    // Search in progress
    data class Success(val tracks: List<Track>) : MusicSearchUiState() // Results found
    data class Error(val message: String) : MusicSearchUiState()       // Error occurred
}
