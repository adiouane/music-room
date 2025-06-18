package com.example.musicroom.presentation.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.Track
import com.example.musicroom.data.service.YouTubeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicSearchViewModel @Inject constructor(
    private val youTubeService: YouTubeService
) : ViewModel() {

    private val _uiState = MutableStateFlow<MusicSearchUiState>(MusicSearchUiState.Empty)
    val uiState: StateFlow<MusicSearchUiState> = _uiState.asStateFlow()

    fun searchTracks(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.value = MusicSearchUiState.Loading
            
            youTubeService.searchTracks(query)
                .onSuccess { tracks ->
                    _uiState.value = if (tracks.isEmpty()) {
                        MusicSearchUiState.Error("No tracks found for '$query'")
                    } else {
                        MusicSearchUiState.Success(tracks)
                    }
                }
                .onFailure { exception ->
                    _uiState.value = MusicSearchUiState.Error(
                        exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }
}

sealed class MusicSearchUiState {
    object Empty : MusicSearchUiState()
    object Loading : MusicSearchUiState()
    data class Success(val tracks: List<Track>) : MusicSearchUiState()
    data class Error(val message: String) : MusicSearchUiState()
}
