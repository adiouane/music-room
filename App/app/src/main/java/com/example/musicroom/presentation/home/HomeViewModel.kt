package com.example.musicroom.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.HomeResponse
import com.example.musicroom.data.service.HomeApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class Success(val homeData: HomeResponse) : HomeState()
    data class Error(val message: String) : HomeState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeApiService: HomeApiService
) : ViewModel() {
    
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Idle)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()
    
    init {
        loadHomeData()
    }
    
    /**
     * Load home screen data from backend
     */
    fun loadHomeData() {
        viewModelScope.launch {
            try {
                _homeState.value = HomeState.Loading
                Log.d("HomeViewModel", "🏠 Loading home data...")
                
                val result = homeApiService.getHomeData()
                
                if (result.isSuccess) {
                    val homeData = result.getOrNull()!!
                    Log.d("HomeViewModel", "✅ Home data loaded successfully")
                    Log.d("HomeViewModel", "📊 Playlists: ${homeData.user_playlists.results.size}")
                    Log.d("HomeViewModel", "🎵 Recommended songs: ${homeData.recommended_songs.results.size}")
                    Log.d("HomeViewModel", "🔥 Popular songs: ${homeData.popular_songs.results.size}")
                    Log.d("HomeViewModel", "🎤 Popular artists: ${homeData.popular_artists.results.size}")
                    
                    _homeState.value = HomeState.Success(homeData)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("HomeViewModel", "❌ Failed to load home data: $error")
                    _homeState.value = HomeState.Error(error)
                }
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Unexpected error loading home data: ${e.message}")
                _homeState.value = HomeState.Error("An unexpected error occurred")
            }
        }
    }
    
    /**
     * Refresh home data
     */
    fun refreshHomeData() {
        loadHomeData()
    }
    
    /**
     * Clear the current state
     */
    fun clearState() {
        _homeState.value = HomeState.Idle
    }
}