package com.example.musicroom.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.musicroom.data.models.MusicPreferences
import com.example.musicroom.presentation.theme.*

@Composable
fun MusicPreferencesDialog(
    musicPreferences: MusicPreferences,
    onDismiss: () -> Unit,
    onSave: (MusicPreferences) -> Unit
) {
    var favoriteGenres by remember { mutableStateOf(musicPreferences.favoriteGenres) }
    var favoriteArtists by remember { mutableStateOf(musicPreferences.favoriteArtists) }
    var musicMood by remember { mutableStateOf(musicPreferences.musicMood) }
    var explicitContent by remember { mutableStateOf(musicPreferences.explicitContent) }

    var newGenre by remember { mutableStateOf("") }
    var newArtist by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Music Preferences",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )

                Text(
                    text = "Customize your music experience",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Favorite Genres Section
                Text(
                    text = "Favorite Genres",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(favoriteGenres) { genre ->
                        FilterChip(
                            onClick = {
                                favoriteGenres = favoriteGenres - genre
                            },
                            label = { Text(genre) },
                            selected = true,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove $genre",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newGenre,
                        onValueChange = { newGenre = it },
                        label = { Text("Add Genre") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = TextSecondary
                        )
                    )
                    Button(
                        onClick = {
                            if (newGenre.isNotBlank() && newGenre !in favoriteGenres) {
                                favoriteGenres = favoriteGenres + newGenre
                                newGenre = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Favorite Artists Section
                Text(
                    text = "Favorite Artists",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(favoriteArtists) { artist ->
                        FilterChip(
                            onClick = {
                                favoriteArtists = favoriteArtists - artist
                            },
                            label = { Text(artist) },
                            selected = true,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove $artist",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {                    OutlinedTextField(
                        value = newArtist,
                        onValueChange = { newArtist = it },
                        label = { Text("Add Artist") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = TextSecondary
                        )
                    )
                    Button(
                        onClick = {
                            if (newArtist.isNotBlank() && newArtist !in favoriteArtists) {
                                favoriteArtists = favoriteArtists + newArtist
                                newArtist = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Music Mood Selection
                Text(
                    text = "Music Mood",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()                ) {
                    val moods = listOf("Happy", "Chill", "Energetic", "Mixed")
                    items(moods) { mood ->
                        FilterChip(
                            onClick = { musicMood = mood },
                            label = { Text(mood) },
                            selected = mood == musicMood
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Explicit Content Setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Allow Explicit Content",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Switch(
                        checked = explicitContent,
                        onCheckedChange = { explicitContent = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryPurple,
                            checkedTrackColor = PrimaryPurple.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            onSave(
                                MusicPreferences(
                                    favoriteGenres = favoriteGenres,
                                    favoriteArtists = favoriteArtists,
                                    musicMood = musicMood,
                                    explicitContent = explicitContent
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
