package com.example.musicroom.presentation.artist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicroom.data.models.Song
import com.example.musicroom.data.models.Artist
import com.example.musicroom.data.service.MusicApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistDetailsViewModel @Inject constructor(
    private val musicApiService: MusicApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ArtistDetailsUiState>(ArtistDetailsUiState.Loading)
    val uiState: StateFlow<ArtistDetailsUiState> = _uiState.asStateFlow()

    fun loadArtistDetails(artistId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ArtistDetailsUiState.Loading
                
                // Create mock artist - TODO: Replace with actual API call
                val artist = createMockArtist(artistId)
                
                // Get artist songs - for now using random songs and filtering by artist name
                // TODO: Replace with actual artist-specific endpoint when backend supports it
                val songsResult = musicApiService.getRandomSongs()
                
                if (songsResult.isSuccess) {
                    val allSongs: List<Song> = songsResult.getOrThrow()
                    
                    // Filter songs by artist name (placeholder logic)
                    // In a real app, this would be done on the backend
                    val artistSongs: List<Song> = allSongs.filter { song: Song ->
                        song.artist_name.contains(artist.name, ignoreCase = true) ||
                        artist.name.contains(song.artist_name, ignoreCase = true)
                    }.ifEmpty {
                        // If no matching songs found, take first 5 random songs as placeholder
                        allSongs.take(5)
                    }
                    
                    Log.d("ArtistDetailsVM", "Loaded ${artistSongs.size} songs for artist ${artist.name}")
                    
                    if (artistSongs.isNotEmpty()) {
                        _uiState.value = ArtistDetailsUiState.Success(artist, artistSongs)
                    } else {
                        _uiState.value = ArtistDetailsUiState.Error("No songs found for this artist")
                    }
                } else {
                    val error = songsResult.exceptionOrNull()
                    Log.e("ArtistDetailsVM", "Failed to fetch songs", error)
                    _uiState.value = ArtistDetailsUiState.Error("Failed to load artist songs: ${error?.message}")
                }
                
            } catch (e: Exception) {
                Log.e("ArtistDetailsVM", "Error loading artist details", e)
                _uiState.value = ArtistDetailsUiState.Error("Failed to load artist details: ${e.message}")
            }
        }
    }
    
    private fun createMockArtist(artistId: String): Artist {
        // Create mock artist based on ID - you can replace this with real API call
        return when (artistId) {
            "5" -> Artist(
                id = "5",
                name = "Both",
                website = "http://www.both-world.com",
                joindate = "2004-07-04",
                image = "https://usercontent.jamendo.com?type=artist&id=5&width=300",
                shorturl = "https://jamen.do/a/5",
                shareurl = "https://www.jamendo.com/artist/5"
            )
            "6" -> Artist(
                id = "6",
                name = "Tryad",
                website = "http://www.tryad.fr",
                joindate = "2004-08-20",
                image = "https://usercontent.jamendo.com?type=artist&id=6&width=300",
                shorturl = "https://jamen.do/a/6",
                shareurl = "https://www.jamendo.com/artist/6"
            )
            "7" -> Artist(
                id = "7",
                name = "Both",
                website = "http://www.both-world.com",
                joindate = "2004-07-04",
                image = "https://usercontent.jamendo.com?type=artist&id=7&width=300",
                shorturl = "https://jamen.do/a/7",
                shareurl = "https://www.jamendo.com/artist/7"
            )
            "9" -> Artist(
                id = "9",
                name = "Shearer",
                website = "http://www.shearer.fr.st",
                joindate = "2004-11-06",
                image = "https://usercontent.jamendo.com?type=artist&id=9&width=300",
                shorturl = "https://jamen.do/a/9",
                shareurl = "https://www.jamendo.com/artist/9"
            )
            "13" -> Artist(
                id = "13",
                name = "Ehma",
                website = "http://www.ehma.net",
                joindate = "2005-01-15",
                image = "https://usercontent.jamendo.com?type=artist&id=13&width=300",
                shorturl = "https://jamen.do/a/13",
                shareurl = "https://www.jamendo.com/artist/13"
            )
            else -> Artist(
                id = artistId,
                name = "Unknown Artist",
                website = null,
                joindate = "2004-01-01",
                image = null,
                shorturl = "https://jamen.do/a/$artistId",
                shareurl = "https://www.jamendo.com/artist/$artistId"
            )
        }
    }
}