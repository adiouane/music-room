package com.example.musicroom.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicroom.data.models.*
import com.example.musicroom.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top bar with refresh button
        TopAppBar(
            title = { 
                Text(
                    "Music Room", 
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ) 
            },
            actions = {
                IconButton(onClick = { viewModel.refreshHomeData() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkBackground
            )
        )
        
        when (val currentState = homeState) {
            is HomeState.Loading -> {
                LoadingScreen()
            }
            is HomeState.Success -> {
                HomeContent(
                    homeData = currentState.homeData,
                    navController = navController,
                    onRefresh = { viewModel.refreshHomeData() }
                )
            }
            is HomeState.Error -> {
                ErrorScreen(
                    message = currentState.message,
                    onRetry = { viewModel.refreshHomeData() }
                )
            }
            is HomeState.Idle -> {
                // Initial state, should quickly transition to Loading
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = PrimaryPurple,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Loading your music...",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "Oops! Something went wrong",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                message,
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun HomeContent(
    homeData: HomeResponse,
    navController: NavController,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome header
        item {
            WelcomeHeader(navController)
        }
        
        // User Playlists Section
        if (homeData.user_playlists.results.isNotEmpty()) {
            item {
                PlaylistSection(
                    title = "Your Playlists",
                    playlists = homeData.user_playlists.results,
                    onPlaylistClick = { playlist ->
                        // Navigate to playlist details
                        navController.navigate("playlist/${playlist.id}")
                    }
                )
            }
        }
        
        // Recommended Songs Section
        if (homeData.recommended_songs.results.isNotEmpty()) {
            item {
                SongsSection(
                    title = "Recommended for You",
                    songs = homeData.recommended_songs.results,
                    onSongClick = { song ->
                        // Navigate to now playing or add to queue
                        navController.navigate("now_playing")
                    }
                )
            }
        }
        
        // Popular Songs Section
        if (homeData.popular_songs.results.isNotEmpty()) {
            item {
                SongsSection(
                    title = "Popular Right Now",
                    songs = homeData.popular_songs.results,
                    onSongClick = { song ->
                        navController.navigate("now_playing")
                    }
                )
            }
        }
        
        // Recently Listened Section
        if (homeData.recently_listened.results.isNotEmpty()) {
            item {
                SongsSection(
                    title = "Recently Listened",
                    songs = homeData.recently_listened.results,
                    onSongClick = { song ->
                        navController.navigate("now_playing")
                    }
                )
            }
        }
        
        // Popular Artists Section
        if (homeData.popular_artists.results.isNotEmpty()) {
            item {
                ArtistsSection(
                    title = "Popular Artists",
                    artists = homeData.popular_artists.results,
                    onArtistClick = { artist ->
                        // Navigate to artist details
                        navController.navigate("artist/${artist.id}")
                    }
                )
            }
        }
        
        // Bottom padding for better scrolling
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WelcomeHeader(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = purpleGradient,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Welcome to MusicRoom",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Discover, create, and share music together",
                color = TextSecondary,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { navController.navigate("music_search") },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎵 Search Music")
            }
        }
    }
}

@Composable
private fun PlaylistSection(
    title: String,
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit
) {
    Column {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) }
                )
            }
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Placeholder for playlist image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        color = PrimaryPurple.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = playlist.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "by ${playlist.user_name}",
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SongsSection(
    title: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs.take(10)) { song -> // Limit to first 10 songs
                SongCard(
                    song = song,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

@Composable
private fun SongCard(
    song: Song,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Song artwork
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.image ?: song.album_image)
                    .crossfade(true)
                    .build(),
                contentDescription = song.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryPurple.copy(alpha = 0.3f)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = song.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = song.artist_name,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = formatDuration(song.duration),
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ArtistsSection(
    title: String,
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
) {
    Column {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(artists) { artist ->
                ArtistCard(
                    artist = artist,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}

@Composable
private fun ArtistCard(
    artist: Artist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Artist image (circular)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artist.image)
                    .crossfade(true)
                    .build(),
                contentDescription = artist.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(PrimaryPurple.copy(alpha = 0.3f)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = artist.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Format duration from seconds to MM:SS format
 */
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
