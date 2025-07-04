package com.example.musicroom.presentation.playlist

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PersonAdd  // Add this import for invite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.example.musicroom.presentation.player.InviteUserDialog  // Add this import
import com.example.musicroom.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever

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
            Log.d("PlaylistTracksVM", "🎵 Loading tracks for playlist: $playlistId")
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
    var showInviteDialog by remember { mutableStateOf(false) }
    
    // Add debug logging
    LaunchedEffect(playlistId) {
        Log.d("PlaylistTracksScreen", "🎵 Loading playlist tracks for ID: $playlistId")
        try {
            viewModel.loadPlaylistTracks(playlistId)
        } catch (e: Exception) {
            Log.e("PlaylistTracksScreen", "❌ Error loading playlist tracks", e)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Bar with better error handling
        TopAppBar(
            title = { 
                Text(
                    text = when (val currentState = uiState) {
                        is PlaylistTracksUiState.Success -> currentState.playlistWithTracks.playlist_info.name
                        else -> "Playlist"
                    },
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { 
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("PlaylistTracksScreen", "❌ Navigation error", e)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            actions = {
                // Add invite button for playlist owners
                if (uiState is PlaylistTracksUiState.Success) {
                    val currentPlaylist = (uiState as PlaylistTracksUiState.Success).playlistWithTracks
                    // You'll need to add an isOwner field to your PlaylistInfo or determine ownership logic
                    IconButton(
                        onClick = { showInviteDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Invite Users",
                            tint = PrimaryPurple
                        )
                    }
                }
                
                // Add delete playlist button (add this to existing actions)
                IconButton(
                    onClick = { /* TODO: implement delete playlist */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Playlist",
                        tint = Color.Red
                    )
                }

                IconButton(
                    onClick = { 
                        try {
                            viewModel.refresh(playlistId)
                        } catch (e: Exception) {
                            Log.e("PlaylistTracksScreen", "❌ Refresh error", e)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Refresh",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface
            )
        )
        
        // Content with enhanced error states
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
                        Text(
                            text = "Playlist ID: $playlistId",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            is PlaylistTracksUiState.Success -> {
                PlaylistContent(
                    playlistWithTracks = currentState.playlistWithTracks,
                    onTrackClick = { track ->
                        try {
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
                        } catch (e: Exception) {
                            Log.e("PlaylistTracksScreen", "❌ Error playing track", e)
                        }
                    },
                    onInviteClick = { showInviteDialog = true }  // Add invite callback
                )
            }
            
            is PlaylistTracksUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
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
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentState.message,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Playlist ID: $playlistId",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { 
                                    try {
                                        viewModel.refresh(playlistId)
                                    } catch (e: Exception) {
                                        Log.e("PlaylistTracksScreen", "❌ Retry error", e)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                            ) {
                                Text("Retry", color = Color.Black)
                            }
                            
                            OutlinedButton(
                                onClick = { 
                                    try {
                                        navController.popBackStack()
                                    } catch (e: Exception) {
                                        Log.e("PlaylistTracksScreen", "❌ Back navigation error", e)
                                    }
                                },
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Text("Go Back", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Invite dialog
    if (showInviteDialog && uiState is PlaylistTracksUiState.Success) {
        val currentPlaylist = (uiState as PlaylistTracksUiState.Success).playlistWithTracks
        InviteUserDialog(
            playlistName = currentPlaylist.playlist_info.name,
            onDismiss = { showInviteDialog = false },
            onInvite = { username ->
                // TODO: Implement invite functionality
                // You can add the invite logic here or in the ViewModel
                Log.d("PlaylistTracksScreen", "🎯 Inviting user: $username to playlist: ${currentPlaylist.playlist_info.name}")
                showInviteDialog = false
                // Show success message or handle invite result
            }
        )
    }
}

@Composable
fun PlaylistContent(
    playlistWithTracks: PlaylistWithTracks,
    onTrackClick: (PlaylistTrackDetails) -> Unit,
    onInviteClick: (() -> Unit)? = null  // Add invite callback
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Playlist Header
        item {
            PlaylistHeaderInfo(
                playlistInfo = playlistWithTracks.playlist_info,
                onInviteClick = onInviteClick  // Pass the callback
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Tracks Section
        item {
            Text(
                text = "Tracks (${playlistWithTracks.tracks.size})",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Track List
        itemsIndexed(playlistWithTracks.tracks) { index, track ->
            PlaylistTrackRow(
                track = track,
                position = index + 1,
                onTrackClick = { onTrackClick(track) }
            )
            if (index < playlistWithTracks.tracks.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = DarkSurface
                )
            }
        }
    }
}

@Composable
fun PlaylistHeaderInfo(
    playlistInfo: com.example.musicroom.data.service.PlaylistInfo,
    onInviteClick: (() -> Unit)? = null  // Add invite callback
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = playlistInfo.name,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Created by ${playlistInfo.owner}",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${playlistInfo.track_count} tracks • ${playlistInfo.followers_count} followers",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Privacy indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (playlistInfo.is_public) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = if (playlistInfo.is_public) "Public" else "Private",
                        tint = if (playlistInfo.is_public) Color.Green else Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (playlistInfo.is_public) "Public" else "Private",
                        color = if (playlistInfo.is_public) Color.Green else Color.Red,
                        fontSize = 12.sp
                    )
                }
                
                // Invite button for playlist owners
                if (onInviteClick != null) {
                    Button(
                        onClick = onInviteClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Invite",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistTrackRow(
    track: PlaylistTrackDetails,
    position: Int,
    onTrackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Position number
        Text(
            text = position.toString(),
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.width(32.dp)
        )
        
        // Track image or icon
        if (track.image.isNotEmpty() || track.album_image.isNotEmpty()) {
            AsyncImage(
                model = track.image.ifEmpty { track.album_image },
                contentDescription = "Track image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback to icon when no image URL
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Music note",
                tint = TextSecondary,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Track info
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
            Text(
                text = track.album_name,
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Duration
        Text(
            text = formatDuration(track.duration),
            color = TextSecondary,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Play button
        IconButton(
            onClick = onTrackClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White
            )
        }
        // Add this to the end of your track row component
        IconButton(
            onClick = { /* TODO: implement delete track */ },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove Track", 
                tint = Color.Red,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Helper function to format duration
fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}

// Helper function to navigate to now playing
fun navigateToNowPlaying(navController: NavController, track: Track) {
    val encodedTitle = URLEncoder.encode(track.title, "UTF-8")
    val encodedArtist = URLEncoder.encode(track.artist, "UTF-8")
    val encodedThumbnailUrl = URLEncoder.encode(track.thumbnailUrl, "UTF-8")
    val encodedDuration = URLEncoder.encode(track.duration, "UTF-8")
    val encodedDescription = URLEncoder.encode(track.description, "UTF-8")
    
    navController.navigate(
        "now_playing/${track.id}/$encodedTitle/$encodedArtist/$encodedThumbnailUrl/$encodedDuration/$encodedDescription"
    )
}