package com.example.musicroom.presentation.player

import androidx.lifecycle.ViewModel
import com.example.musicroom.data.models.Track
import com.example.musicroom.data.service.RepeatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor() : ViewModel() {
    
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(180f)
    val duration: StateFlow<Float> = _duration.asStateFlow()
    
    private val _isPlayerReady = MutableStateFlow(false)
    val isPlayerReady: StateFlow<Boolean> = _isPlayerReady.asStateFlow()
    
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    fun playTrack(track: Track) {
        _currentTrack.value = track
        _isPlayerReady.value = true
        _isPlaying.value = true
        // Simulate playback for now
    }
    
    fun play() {
        _isPlaying.value = true
    }
    
    fun pause() {
        _isPlaying.value = false
    }
    
    fun seekTo(position: Float) {
        _currentPosition.value = position
    }
    
    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }
    
    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }
}
