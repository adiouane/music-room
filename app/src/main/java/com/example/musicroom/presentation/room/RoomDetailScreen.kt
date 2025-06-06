package com.example.musicroom.presentation.room

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicroom.presentation.theme.*
import com.example.musicroom.presentation.home.LiveIndicator
import com.example.musicroom.data.models.MusicRoom
import com.example.musicroom.data.models.Track
import com.example.musicroom.data.models.activeRooms

@Composable
fun RoomDetailScreen(
    roomId: String?,
    onBackClick: () -> Unit
) {
    // Use remember to store the room state
    val room = remember(roomId) { 
        roomId?.let { id -> activeRooms.find { it.id == id } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        room?.let { safeRoom ->
            RoomHeader(room = safeRoom)
            CurrentTrackPlayer(
                track = safeRoom.playlist.firstOrNull()
            )
            PlaylistSection(playlist = safeRoom.playlist)
            RoomControls()
        } ?: run {
            // Show error state when room is not found
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Room not found",
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun RoomHeader(room: MusicRoom) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    room.name,
                    color = TextPrimary,
                    fontSize = 20.sp
                )
                Row {
                    LiveIndicator(isLive = room.isLive)
                    Text(
                        "${room.listeners} listening",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentTrackPlayer(track: Track?) {
    track?.let { safeTrack ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Track Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Current Track",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            "${safeTrack.title} - ${safeTrack.artist}",
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    }
                    
                    // Player Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.SkipPrevious, null, tint = TextPrimary)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.PlayArrow, null, tint = TextPrimary)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.SkipNext, null, tint = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistSection(playlist: List<Track>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Playlist",
            color = TextPrimary,
            fontSize = 20.sp
        )
        
        LazyColumn {
            itemsIndexed(playlist) { index, track ->
                PlaylistItem(track = track, index = index)
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    track: Track,
    index: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${index + 1}",
                color = TextSecondary,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.title,
                    color = TextPrimary,
                    fontSize = 16.sp
                )
                Text(
                    track.artist,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
            
            Row {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ThumbDown, null, tint = TextSecondary)
                }
                Text(
                    track.votes.toString(),
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ThumbUp, null, tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun RoomControls() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Add, null, tint = TextPrimary)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, null, tint = TextPrimary)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.VolumeUp, null, tint = TextPrimary)
            }
        }
    }
}