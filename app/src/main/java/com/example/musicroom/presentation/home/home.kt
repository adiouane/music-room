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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import com.example.musicroom.R
import com.example.musicroom.presentation.theme.*
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow

// Data model
data class Album(val title: String, val artist: String, val imageRes: Int)

data class Artist(val name: String, val imageRes: Int)

data class MusicRoom(
    val id: String,
    val name: String,
    val hostName: String,
    val currentTrack: String,
    val listeners: Int,
    val isLive: Boolean
)

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

val activeRooms = listOf(
    MusicRoom(
        id = "1",
        name = "Chill Vibes Room",
        hostName = "LFERDA",
        currentTrack = "Figoshin - New Track",
        listeners = 42,
        isLive = true
    ),
    MusicRoom(
        id = "2",
        name = "Party Mix",
        hostName = "Cheb Mami",
        currentTrack = "Live Session",
        listeners = 28,
        isLive = true
    )
)

@Composable
fun HomeTabScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
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

        item {
            Text(
                "Live Rooms",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(activeRooms) { room ->
                    RoomCard(room = room)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                "Expand your recent listening",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(continueAlbums) { album ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = album.imageRes),
                            contentDescription = album.title,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(album.title, color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text("Made For You", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(trackList) { track -> 
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
            Spacer(modifier = Modifier.height(16.dp))
            Text("Discover picks for you", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(trackList) { track -> 
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
                                    Text("Hand-picked for you", color = TextSecondary, fontSize = 12.sp)
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
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
        }
    }
}

@Composable
private fun LiveIndicator() {
    val transition = remember { 
        Animatable(0.8f)
    }
    
    LaunchedEffect(Unit) {
        transition.animateTo(
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(transition.value)
            .background(Color(0xFF4CAF50), CircleShape)
    )
}


@Composable
private fun RoomCard(
    room: MusicRoom,
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
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E4620)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LiveIndicator()
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