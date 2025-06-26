package com.example.musicroom.data.service

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.musicroom.data.models.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayerService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val freeAudioService: FreeAudioService
) {
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    
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
        _currentPosition.value = 0f
        _isPlayerReady.value = false
        
        Log.d("MusicPlayer", "🎵 Loading track: ${track.title} by ${track.artist}")
        
        // Release existing MediaPlayer
        releaseMediaPlayer()
        
        serviceScope.launch {
            try {
                // Find free audio for this track
                val audioUrl = freeAudioService.findFreeAudioForTrack(track)
                
                if (audioUrl != null) {
                    Log.d("MusicPlayer", "🎧 Found audio: $audioUrl")
                    playAudioFromUrl(audioUrl, track)
                } else {
                    Log.w("MusicPlayer", "⚠️ No audio found, using simulation")
                    startSimulatedPlayback()
                }
                
            } catch (e: Exception) {
                Log.e("MusicPlayer", "❌ Error loading track: ${e.message}")
                startSimulatedPlayback()
            }
        }
    }
    
    private suspend fun playAudioFromUrl(audioUrl: String, track: Track) {
        withContext(Dispatchers.Main) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioUrl)
                    
                    setOnPreparedListener { mp ->
                        val durationSeconds = if (mp.duration > 0) {
                            mp.duration.toFloat() / 1000f
                        } else {
                            180f // Default 3 minutes if unknown
                        }
                        
                        _duration.value = durationSeconds
                        _isPlayerReady.value = true
                        mp.start()
                        _isPlaying.value = true
                        startProgressTracking()
                        
                        Log.d("MusicPlayer", "✅ Playing: ${track.title} (${durationSeconds}s)")
                    }
                    
                    setOnCompletionListener {
                        _isPlaying.value = false
                        handleTrackCompletion()
                        Log.d("MusicPlayer", "🏁 Track completed: ${track.title}")
                    }
                    
                    setOnErrorListener { _, what, extra ->
                        Log.e("MusicPlayer", "❌ MediaPlayer error: what=$what, extra=$extra")
                        // Fallback to simulation
                        serviceScope.launch { startSimulatedPlayback() }
                        true
                    }
                    
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("MusicPlayer", "❌ Error creating MediaPlayer: ${e.message}")
                startSimulatedPlayback()
            }
        }
    }
    
    private fun startSimulatedPlayback() {
        _isPlayerReady.value = true
        _isPlaying.value = true
        _duration.value = 180f
        startProgressTracking()
        Log.d("MusicPlayer", "🎭 Started simulation mode")
    }
    
    fun play() {
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.start()
                _isPlaying.value = true
                startProgressTracking()
                Log.d("MusicPlayer", "▶️ Resumed real playback")
            }
        } ?: run {
            _isPlaying.value = true
            startProgressTracking()
            Log.d("MusicPlayer", "▶️ Resumed simulation")
        }
    }
    
    fun pause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                _isPlaying.value = false
                progressJob?.cancel()
                Log.d("MusicPlayer", "⏸️ Paused real playback")
            }
        } ?: run {
            _isPlaying.value = false
            progressJob?.cancel()
            Log.d("MusicPlayer", "⏸️ Paused simulation")
        }
    }
    
    fun seekTo(position: Float) {
        mediaPlayer?.let { mp ->
            val positionMs = (position * 1000).toInt()
            mp.seekTo(positionMs)
            _currentPosition.value = position
            Log.d("MusicPlayer", "⏩ Real seek to: ${position}s")
        } ?: run {
            _currentPosition.value = position
            Log.d("MusicPlayer", "⏩ Simulation seek to: ${position}s")
        }
    }
    
    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (_isPlaying.value) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPosition.value = mp.currentPosition.toFloat() / 1000f
                    }
                } ?: run {
                    // Simulation mode
                    val currentPos = _currentPosition.value
                    val duration = _duration.value
                    if (currentPos < duration) {
                        _currentPosition.value = currentPos + 0.1f
                    } else {
                        handleTrackCompletion()
                        return@launch
                    }
                }
                delay(100)
            }
        }
    }
    
    private fun handleTrackCompletion() {
        _isPlaying.value = false
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                _currentPosition.value = 0f
                serviceScope.launch {
                    delay(500)
                    _currentTrack.value?.let { playTrack(it) }
                }
            }
            RepeatMode.ALL -> {
                _currentPosition.value = 0f
            }
            RepeatMode.OFF -> {
                _currentPosition.value = 0f
            }
        }
    }
    
    private fun releaseMediaPlayer() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            } catch (e: Exception) {
                Log.e("MusicPlayer", "Error releasing MediaPlayer: ${e.message}")
            }
            mediaPlayer = null
        }
        progressJob?.cancel()
    }
    
    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        Log.d("MusicPlayer", "🔀 Shuffle: ${_isShuffleEnabled.value}")
    }
    
    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        Log.d("MusicPlayer", "🔁 Repeat: ${_repeatMode.value}")
    }
    
    fun release() {
        releaseMediaPlayer()
    }
}

enum class RepeatMode {
    OFF, ALL, ONE
}
