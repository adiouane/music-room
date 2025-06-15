package com.example.musicroom.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.Artist
import com.example.musicroom.data.models.Song
import com.example.musicroom.data.repository.ArtistRepository
import com.example.musicroom.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val artistRepository = ArtistRepository()
    private val songRepository = SongRepository()
    
    private val _popularArtists = MutableStateFlow<List<Artist>>(emptyList())
    val popularArtists: StateFlow<List<Artist>> = _popularArtists.asStateFlow()
    
    private val _recommendedSongs = MutableStateFlow<List<Song>>(emptyList())
    val recommendedSongs: StateFlow<List<Song>> = _recommendedSongs.asStateFlow()
    
    private val _popularSongs = MutableStateFlow<List<Song>>(emptyList())
    val popularSongs: StateFlow<List<Song>> = _popularSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _popularArtists.value = artistRepository.getPopularArtists()
                _recommendedSongs.value = songRepository.getRecommendedSongs()
                _popularSongs.value = songRepository.getPopularSongs()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}