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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicroom.R

// Data model
data class Album(val title: String, val artist: String, val imageRes: Int)

data class Artist(val name: String, val imageRes: Int)

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

@Composable
fun HomeTabScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1DB954), Color(0xFF121212))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = "Home",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("Expand your recent listening", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                        Text(album.title, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text("Made For You", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(trackList) { track ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
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
                        Text(track.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(track.artist, color = Color.Gray, fontSize = 12.sp)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Discover picks for you", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(trackList) { track ->
                    Card(
                        modifier = Modifier.width(260.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))
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
                                    Text(track.title, color = Color.White, fontSize = 14.sp)
                                    Text("Hand-picked for you", color = Color.Gray, fontSize = 12.sp)
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Popular Artists", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                        Text(artist.name, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
