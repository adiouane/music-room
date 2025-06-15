package com.example.musicroom.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import com.example.musicroom.R
import com.example.musicroom.presentation.theme.*
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import com.example.musicroom.data.models.Track
import com.example.musicroom.data.models.MusicRoom
import com.example.musicroom.data.models.activeRooms
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import com.example.musicroom.data.models.Artist
import androidx.compose.runtime.getValue
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon

// Data model
data class Album(val title: String, val artist: String, val imageRes: Int)

data class Playlist(val id: String, val name: String, val trackCount: Int, val imageRes: Int)
data class Event(val name: String, val date: String, val venue: String, val imageRes: Int)

// Remove the duplicate Song data class since it already exists in models

// Dummy data
val recentPlaylists = listOf(
    Playlist("1", "Today's Top Hits", 50, R.drawable.todays_hits),
    Playlist("2", "Rock Classics", 42, R.drawable.rock_classics),
    Playlist("3", "Hip Hop Central", 38, R.drawable.hiphop_central)
)

val upcomingEvents = listOf(
    Event("Summer Music Festival", "Aug 10", "Central Park", R.drawable.summer_festival),
    Event("Jazz Night", "Aug 20", "Blue Note", R.drawable.jazz_night),
    Event("Indie Rock Concert", "Aug 25", "The Venue", R.drawable.indie_concert)
)

val continueAlbums = listOf(
    Album("Short 'n Sweet", "Sabrina Carpenter", R.drawable.short_sweet),
    Album("Fireworks & Rollerblades", "Benson Boone", R.drawable.fireworks_rollerblades),
    Album("BRAT", "Charli XCX", R.drawable.brat)
)

// Image state sealed class
sealed class ImageState {
    object Loading : ImageState()
    object Success : ImageState()
    object Error : ImageState()
}

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()
    val apiArtists by viewModel.popularArtists.collectAsState()
    val recommendedSongs by viewModel.recommendedSongs.collectAsState()
    val populardSongs by viewModel.popularSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = purpleGradient,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Column {
                    Text(
                        text = "Welcome to MusicRoom",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Discover and share your favorite music",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. Your Playlists Section
        item {
            Text("Your Playlists", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recentPlaylists) { playlist ->
                    PlaylistCard(playlist = playlist, navController = navController)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. Recommended Songs Section
        item {
            Text("Recommended Songs", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (recommendedSongs.isEmpty()) {
                Text(
                    "No songs available",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(recommendedSongs) { song -> 
                        SongItem(
                            song = song,
                            onSongClick = {
                                // Handle song click - maybe navigate to player or start playing
                                navController.navigate("player/${song.id}")
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. Popular Songs Section
        item {
            Text("Popular Songs", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (populardSongs.isEmpty()) {
                Text(
                    "No songs available",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(populardSongs) { song -> 
                        SongItem(
                            song = song,
                            onSongClick = {
                                // Handle song click - maybe navigate to player or start playing
                                navController.navigate("player/${song.id}")
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. Recently Listened Section
        item {
            Text("Recently Listened", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(continueAlbums) { album ->
                    AlbumCard(album = album)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 6. Popular Artists Section
        item {
            Text("Popular Artists", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (apiArtists.isEmpty()) {
                Text(
                    "No artists available",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(apiArtists) { artist -> 
                        ArtistItem(
                            artist = artist,
                            onArtistClick = {
                                // Handle artist click - maybe navigate to artist details
                                navController.navigate("artist/${artist.id}")
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }


        // 6. Events Section
        item {
            Text("Events", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activeRooms.take(5)) { room ->
                    RoomCard(room = room, navController = navController)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

    }
}

// Helper function to truncate text if longer than specified length
fun truncateText(text: String, maxLength: Int = 14): String {
    return if (text.length > maxLength) {
        text.take(maxLength) + "..."
    } else {
        text
    }
}

@Composable
fun SongItem(
    song: com.example.musicroom.data.models.Song,
    onSongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .height(260.dp)
            .clickable(onClick = onSongClick)
    ) {
        // Album cover image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            var imageState by remember { mutableStateOf<ImageState>(ImageState.Loading) }
            
            when (imageState) {
                is ImageState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
                is ImageState.Error -> {
                    Icon(
                        painter = painterResource(id = R.drawable.default_album_image),
                        contentDescription = "Default album",
                        modifier = Modifier.size(50.dp),
                        tint = Color.Gray
                    )
                }
                is ImageState.Success -> {
                    // This will be shown when AsyncImage loads successfully
                }
            }
            
            // Load the actual image
            if (song.image.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.image)
                        .crossfade(true)
                        .listener(
                            onStart = { imageState = ImageState.Loading },
                            onSuccess = { _, _ -> imageState = ImageState.Success },
                            onError = { _, _ -> imageState = ImageState.Error }
                        )
                        .build(),
                    contentDescription = song.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LaunchedEffect(Unit) {
                    imageState = ImageState.Error
                }
            }
            
            // Play button overlay
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play_arrow),
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Song name - truncated if longer than 14 characters
        Text(
            text = truncateText(song.name, 14),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Artist name - truncated if longer than 14 characters
        Text(
            text = truncateText(song.artistName, 14),
            color = TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        // Duration
        Text(
            text = formatDuration(song.duration),
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Helper function to format duration
fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

@Composable
fun ArtistItem(
    artist: Artist,
    onArtistClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .padding(4.dp)
            .clickable(onClick = onArtistClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artist image
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            var imageState by remember { mutableStateOf<ImageState>(ImageState.Loading) }
            
            when (imageState) {
                is ImageState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
                is ImageState.Error -> {
                    Icon(
                        painter = painterResource(id = R.drawable.default_album_image),
                        contentDescription = "Default artist",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )
                }
                is ImageState.Success -> {
                    // This will be shown when AsyncImage loads successfully
                }
            }
            
            // Load the actual image
            if (artist.image.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artist.image)
                        .crossfade(true)
                        .listener(
                            onStart = { imageState = ImageState.Loading },
                            onSuccess = { _, _ -> imageState = ImageState.Success },
                            onError = { _, _ -> imageState = ImageState.Error }
                        )
                        .build(),
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LaunchedEffect(Unit) {
                    imageState = ImageState.Error
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Artist name
        Text(
            text = artist.name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun LiveIndicator(isLive: Boolean = false) {
    if (isLive) {
        Card(
            modifier = Modifier
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E4620)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color.Green, CircleShape)
                )
                Text(
                    "LIVE",
                    color = Color.Green,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RoomCard(
    room: MusicRoom,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable { 
                navController.navigate("room_detail/${room.id}")
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        room.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Hosted by ${room.host}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                LiveIndicator(room.isLive)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "♪ ${room.currentTrack ?: "No track playing"}",
                color = TextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${room.listeners} listening",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist, navController: NavController) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .clickable { 
                navController.navigate("playlist_details/${playlist.id}")
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = playlist.imageRes),
                contentDescription = playlist.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                playlist.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${playlist.trackCount} tracks",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun TrackCard(track: Album) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = track.imageRes),
                contentDescription = track.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                track.title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                track.artist,
                color = TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    val cardWidth = when {
        screenWidth > 600.dp -> screenWidth * 0.3f
        screenWidth > 400.dp -> screenWidth * 0.45f
        else -> screenWidth * 0.75f
    }

    // Animation for the live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "live_animation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    Card(
        modifier = modifier
            .clickable { 
                // Navigate to event details
            }
            .width(cardWidth)
            .height(160.dp)
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Event Type Badge with animated live indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                Card(
                    modifier = Modifier
                        .padding(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Outer glow effect
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .scale(scale)
                                    .alpha(alpha * 0.3f)
                                    .background(
                                        Color.Green.copy(alpha = 0.3f), 
                                        CircleShape
                                    )
                            )
                            // Inner circle
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .alpha(alpha)
                                    .background(Color.Green, CircleShape)
                            )
                        }
                        Text(
                            "LIVE",
                            color = Color.Green,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        event.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        event.date,
                        color = PrimaryPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            event.venue,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumCard(album: Album) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = album.imageRes),
                contentDescription = album.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                album.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                album.artist,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
