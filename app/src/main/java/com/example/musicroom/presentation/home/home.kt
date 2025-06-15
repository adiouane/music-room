package com.example.musicroom.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

// Data model
data class Album(val title: String, val artist: String, val imageRes: Int)

data class Artist(val name: String, val imageRes: Int)

data class Playlist(val id: String, val name: String, val trackCount: Int, val imageRes: Int)
data class Event(val name: String, val date: String, val venue: String, val imageRes: Int)

// Dummy data
val continueAlbums = listOf(
    Album("Short 'n Sweet", "Sabrina Carpenter", R.drawable.short_sweet),
    Album("Fireworks & Rollerblades", "Benson Boone", R.drawable.fireworks_rollerblades),
    Album("BRAT", "Charli XCX", R.drawable.brat)
)

val trackList = listOf(
    Album("Wake Up", "Imagine Dragons", R.drawable.wake_up),
    Album("LUNCH", "Billie Eilish", R.drawable.lunch),
    Album("Good Luck, Babe!", "Chappell Roan", R.drawable.good_luck_babe),
    Album("Espresso", "Sabrina Carpenter", R.drawable.espresso)
)

val popularArtists = listOf(
    Artist("LFERDA", R.drawable.short_sweet),
    Artist("Figoshin", R.drawable.fireworks_rollerblades),
    Artist("Cheb Mami", R.drawable.brat)
)

val userPlaylists = listOf(
    Playlist("1", "My Favorites", 25, R.drawable.short_sweet),
    Playlist("2", "Workout Mix", 18, R.drawable.fireworks_rollerblades),
    Playlist("3", "Chill Vibes", 32, R.drawable.brat)
)

val recentlyListened = listOf(
    Album("Short 'n Sweet", "Sabrina Carpenter", R.drawable.short_sweet),
    Album("Fireworks & Rollerblades", "Benson Boone", R.drawable.fireworks_rollerblades),
    Album("BRAT", "Charli XCX", R.drawable.brat)
)

val recommendedSongs = listOf(
    Album("Espresso", "Sabrina Carpenter", R.drawable.espresso),
    Album("Good Luck, Babe!", "Chappell Roan", R.drawable.good_luck_babe),
    Album("LUNCH", "Billie Eilish", R.drawable.lunch)
)

val popularSongs = listOf(
    Album("Wake Up", "Imagine Dragons", R.drawable.wake_up),
    Album("LUNCH", "Billie Eilish", R.drawable.lunch),
    Album("Good Luck, Babe!", "Chappell Roan", R.drawable.good_luck_babe)
)

val upcomingEvents = listOf(
    Event("Summer Music Festival", "July 15, 2024", "Central Park", R.drawable.short_sweet),
    Event("Jazz Night", "July 20, 2024", "Blue Note", R.drawable.fireworks_rollerblades),
    Event("Rock Concert", "July 25, 2024", "Madison Square Garden", R.drawable.brat)
)

@Composable
fun HomeTabScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
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

        // 1. User Playlists Section
        item {
            Text(
                "Your Playlists",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(userPlaylists) { playlist ->
                    PlaylistCard(playlist = playlist, navController = navController)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. Recommended Songs Section (Vertical list)
        item {
            Text(
                "Recommended for You",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(recommendedSongs) { track -> 
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Image(
                        painter = painterResource(id = track.imageRes),
                        contentDescription = track.title,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            track.title,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            track.artist,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. Popular Songs Section (Horizontal scrollable)
        item {
            Text("Popular Songs", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(popularSongs) { track -> 
                    Card(
                        modifier = Modifier.width(260.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = track.imageRes),
                                    contentDescription = track.title,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(track.title, color = TextPrimary, fontSize = 14.sp)
                                    Text(track.artist, color = TextSecondary, fontSize = 12.sp)
                                    Text("Trending", color = TextSecondary, fontSize = 12.sp)
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. Recently Listened Section
        item {
            Text(
                "Recently Listened",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recentlyListened) { track ->
                    TrackCard(track = track)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. Popular Artists Section
        item {
            Text("Popular Artists", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(popularArtists) { artist -> 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = artist.imageRes),
                            contentDescription = artist.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(artist.name, color = TextPrimary, fontSize = 12.sp)
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
                items(upcomingEvents) { event ->
                    EventCard(event = event)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
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
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    val cardWidth = when {
        screenWidth > 600.dp -> screenWidth * 0.3f
        screenWidth > 400.dp -> screenWidth * 0.45f
        else -> screenWidth * 0.75f
    }

    Card(
        modifier = modifier
            .clickable { 
                navController.navigate("room/${room.id}")
            }
            .width(cardWidth)
            .height(160.dp)
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Live Indicator Badge
            if (room.isLive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    LiveIndicator(isLive = room.isLive)
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = room.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Hosted by ${room.hostName}",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = room.currentTrack,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
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
                            // Main circle
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = event.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "at ${event.venue}",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = event.date,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "View Details",
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
    }
}
