package com.example.musicroom.presentation.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicroom.data.models.User
import com.example.musicroom.presentation.theme.*

data class Track(
    val title: String,
    val artist: String,
    val imageUrl: String? = null
)

data class SettingsItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun ProfileScreen(user: User) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header Section with Edit Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = purpleGradient)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Picture with Edit Option
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.Center),
                            tint = TextPrimary
                        )
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = TextPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = user.name, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Text(text = "@${user.username}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }

        // Privacy Settings Section
        item {
            SettingsSection(
                title = "Privacy Settings",
                items = listOf(
                    SettingsItem("Profile Visibility", Icons.Default.Visibility),
                    SettingsItem("Who Can See My Activity", Icons.Default.People),
                    SettingsItem("Blocked Users", Icons.Default.Block)
                )
            )
        }

        // Account Settings Section
        item {
            SettingsSection(
                title = "Account Settings",
                items = listOf(
                    SettingsItem("Change Password", Icons.Default.Lock),
                    SettingsItem("Connected Devices", Icons.Default.Devices),
                    SettingsItem("Notification Settings", Icons.Default.Notifications)
                )
            )
        }

        // Music Preferences Section
        item {
            SettingsSection(
                title = "Music Preferences",
                items = listOf(
                    SettingsItem("Favorite Genres", Icons.Default.Favorite),
                    SettingsItem("Listening History", Icons.Default.History),
                    SettingsItem("Playlists", Icons.AutoMirrored.Filled.QueueMusic)
                )
            )
        }

        // Logout Button
        item {
            Button(
                onClick = { /* Handle logout */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, items: List<SettingsItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsItemRow(item)
                    if (index < items.size - 1) {
                        Divider(color = TextSecondary.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(item: SettingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = TextPrimary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}