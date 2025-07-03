package com.example.musicroom.presentation.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.musicroom.data.models.Track
import com.example.musicroom.data.service.PlaylistApiService
import com.example.musicroom.data.service.PlaylistWithTracks
import com.example.musicroom.data.service.PlaylistTrackDetails
import com.example.musicroom.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

// UI State
sealed class PlaylistTracksUiState {
    object Loading : PlaylistTracksUiState()
    data class Success(val playlistWithTracks: PlaylistWithTracks) : PlaylistTracksUiState()
    data class Error(val message: String) : PlaylistTracksUiState()
}

// ViewModel
@HiltViewModel
class PlaylistTracksViewModel @Inject constructor(
    private val playlistApiService: PlaylistApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PlaylistTracksUiState>(PlaylistTracksUiState.Loading)
    val uiState: StateFlow<PlaylistTracksUiState> = _uiState.asStateFlow()
    
    fun loadPlaylistTracks(playlistId: String) {
        viewModelScope.launch {
            _uiState.value = PlaylistTracksUiState.Loading
            
            playlistApiService.getPlaylistTracks(playlistId).fold(
                onSuccess = { playlistWithTracks ->
                    _uiState.value = PlaylistTracksUiState.Success(playlistWithTracks)
                },
                onFailure = { exception ->
                    _uiState.value = PlaylistTracksUiState.Error(
                        exception.message ?: "Failed to load playlist tracks"
                    )
                }
            )
        }
    }
    
    fun refresh(playlistId: String) {
        loadPlaylistTracks(playlistId)
    }
}

// Main Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistTracksScreen(
    playlistId: String,
    navController: NavController,
    viewModel: PlaylistTracksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistTracks(playlistId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Playlist", color = TextPrimary) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refresh(playlistId) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface
            )
        )
        
        // Content
        when (val currentState = uiState) {
            is PlaylistTracksUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading playlist...",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            is PlaylistTracksUiState.Success -> {
                PlaylistContent(
                    playlistWithTracks = currentState.playlistWithTracks,
                    onTrackClick = { track ->
                        // Convert PlaylistTrackDetails to Track and navigate
                        val convertedTrack = Track(
                            id = track.id,
                            title = track.name,
                            artist = track.artist_name,
                            thumbnailUrl = track.image.ifEmpty { track.album_image },
                            duration = formatDuration(track.duration),
                            channelTitle = track.album_name,
                            description = track.audio // Store audio URL
                        )
                        navigateToNowPlaying(navController, convertedTrack)
                    }
                )
            }
            
            is PlaylistTracksUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "😔",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Failed to load playlist",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentState.message,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.refresh(playlistId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Retry", color = DarkBackground)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistContent(
    playlistWithTracks: PlaylistWithTracks,
    onTrackClick: (PlaylistTrackDetails) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Playlist Header
        item {
            PlaylistHeader(playlistWithTracks.playlist_info)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Tracks Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tracks (${playlistWithTracks.tracks.size})",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (playlistWithTracks.tracks.isNotEmpty()) {
                    Button(
                        onClick = { 
                            // Play all tracks
                            if (playlistWithTracks.tracks.isNotEmpty()) {
                                onTrackClick(playlistWithTracks.tracks.first())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.size(width = 100.dp, height = 36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = DarkBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Play All",
                            color = DarkBackground,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Track List
        if (playlistWithTracks.tracks.isEmpty()) {
            item {
                EmptyPlaylistMessage()
            }
        } else {
            itemsIndexed(playlistWithTracks.tracks) { index, track ->
                PlaylistTrackItem(
                    track = track,
                    position = index + 1,
                    onClick = { onTrackClick(track) }
                )
            }
        }
        
        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun PlaylistHeader(playlistInfo: com.example.musicroom.data.service.PlaylistInfo) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Playlist Image placeholder
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Playlist Name
        Text(
            text = playlistInfo.name,
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Owner and privacy
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "By ${playlistInfo.owner}",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (playlistInfo.is_public) Icons.Default.Public else Icons.Default.Lock,
                contentDescription = if (playlistInfo.is_public) "Public" else "Private",
                tint = if (playlistInfo.is_public) Color.Green else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Stats
        Text(
            text = "${playlistInfo.track_count} songs • ${playlistInfo.followers_count} followers",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PlaylistTrackItem(
    track: PlaylistTrackDetails,
    position: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Position
            Text(
                text = position.toString(),
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.width(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track Image
            AsyncImage(
                model = track.image.ifEmpty { track.album_image },
                contentDescription = track.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.name,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist_name,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (track.album_name.isNotBlank()) {
                    Text(
                        text = track.album_name,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Duration
            Text(
                text = formatDuration(track.duration),
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EmptyPlaylistMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎵",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tracks yet",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "This playlist is waiting for its first song",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Helper functions
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

private fun navigateToNowPlaying(navController: NavController, track: Track) {
    try {
        val encodedTitle = URLEncoder.encode(track.title, "UTF-8")
        val encodedArtist = URLEncoder.encode(track.artist, "UTF-8")
        val encodedThumbnailUrl = URLEncoder.encode(track.thumbnailUrl, "UTF-8")
        val encodedDuration = URLEncoder.encode(track.duration, "UTF-8")
        val encodedDescription = URLEncoder.encode(track.description, "UTF-8")
        
        navController.navigate("now_playing/${track.id}/$encodedTitle/$encodedArtist/$encodedThumbnailUrl/$encodedDuration/$encodedDescription")
    } catch (e: Exception) {
        // Handle navigation error
        println("Navigation error: ${e.message}")
    }
}