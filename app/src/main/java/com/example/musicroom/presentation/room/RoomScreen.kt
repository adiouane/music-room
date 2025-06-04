package com.example.musicroom.presentation.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicroom.data.models.*
import com.example.musicroom.presentation.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown

@Composable
fun RoomScreen(
    room: Room,
    onVote: (Track, Int) -> Unit,
    onAddTrack: (Track) -> Unit,
    onDevicePermissionChange: (Device, DevicePermissions) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        RoomHeader(room)
        CurrentTrackSection(room.playlist.firstOrNull())
        PlaylistSection(
            tracks = room.playlist,
            onVote = onVote
        )
    }
}

@Composable
private fun RoomHeader(room: Room) {
    Text(
        text = room.name,
        style = MaterialTheme.typography.headlineMedium,
        color = TextPrimary
    )
}

@Composable
private fun CurrentTrackSection(track: Track?) {
    track?.let {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = it.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PlaylistSection(
    tracks: List<Track>,
    onVote: (Track, Int) -> Unit
) {
    LazyColumn {
        items(tracks) { track ->
            TrackItem(track = track, onVote = onVote)
        }
    }
}

@Composable
private fun TrackItem(
    track: Track,
    onVote: (Track, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.titleMedium)
                Text(track.artist, style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = { onVote(track, -1) }) {
                    Icon(Icons.Default.ThumbDown, null)
                }
                Text(
                    text = track.votes.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = { onVote(track, 1) }) {
                    Icon(Icons.Default.ThumbUp, null)
                }
            }
        }
    }
}