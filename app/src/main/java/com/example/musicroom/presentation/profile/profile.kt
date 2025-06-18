package com.example.musicroom.presentation.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicroom.data.models.User
import com.example.musicroom.data.models.UserProfile
import com.example.musicroom.data.models.PublicInfo
import com.example.musicroom.data.models.FriendsInfo
import com.example.musicroom.data.models.PrivateInfo
import com.example.musicroom.data.models.MusicPreferences
import com.example.musicroom.presentation.theme.*

enum class ProfileSection {
    PUBLIC,
    FRIENDS_INFO,
    PRIVATE,
    MUSIC_PREFERENCES
}

@Composable
fun ProfileScreen(user: User) {
    var selectedSection by remember { mutableStateOf<ProfileSection?>(null) }
      // Mock user profile - in real app this would come from repository
    var userProfile by remember {
        mutableStateOf(
            UserProfile(
                userId = user.id,
                publicInfo = PublicInfo(
                    displayName = user.name,
                    username = user.username,
                    bio = "Music lover and playlist creator",
                    profilePictureUrl = user.photoUrl
                ),
                friendsInfo = FriendsInfo(
                    email = user.email,
                    realName = user.name,
                    location = "New York, NY"
                ),
                privateInfo = PrivateInfo(
                    phoneNumber = "+1 (555) 123-4567",
                    birthDate = "January 15, 1995",
                    notes = "Private thoughts about music..."
                ),
                musicPreferences = MusicPreferences(
                    favoriteGenres = listOf("Rock", "Pop", "Jazz", "Electronic"),
                    favoriteArtists = listOf("The Beatles", "Daft Punk", "Miles Davis"),
                    musicMood = "Mixed",
                    explicitContent = false
                )
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {        // Header Section
        item {
            ProfileHeader(
                userProfile = userProfile
            )
        }

        // Profile Information Sections
        item {
            ProfileInfoSection(
                title = "Public Information",
                subtitle = "Visible to everyone",                icon = Icons.Default.Public,
                onClick = { selectedSection = ProfileSection.PUBLIC }
            )
        }

        item {
            ProfileInfoSection(
                title = "Friends Only Information",
                subtitle = "Only your friends can see this",
                icon = Icons.Default.People,
                onClick = { selectedSection = ProfileSection.FRIENDS_INFO }
            )
        }

        item {
            ProfileInfoSection(
                title = "Private Information",
                subtitle = "Only visible to you",
                icon = Icons.Default.Lock,
                onClick = { selectedSection = ProfileSection.PRIVATE }
            )
        }

        item {
            ProfileInfoSection(
                title = "Music Preferences",
                subtitle = "Your musical tastes and settings",
                icon = Icons.Default.MusicNote,
                onClick = { selectedSection = ProfileSection.MUSIC_PREFERENCES }
            )
        }

        // Logout Button
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { /* Handle logout */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }
        }    }

    // Section Detail Dialogs
    selectedSection?.let { section ->        when (section) {
            ProfileSection.PUBLIC -> {
                EditPublicInfoDialog(
                    publicInfo = userProfile.publicInfo,
                    onDismiss = { selectedSection = null },
                    onSave = { updatedInfo ->
                        userProfile = userProfile.copy(publicInfo = updatedInfo)
                        selectedSection = null
                    }
                )
            }
            ProfileSection.FRIENDS_INFO -> {
                EditFriendsInfoDialog(
                    friendsInfo = userProfile.friendsInfo,
                    onDismiss = { selectedSection = null },
                    onSave = { updatedInfo ->
                        userProfile = userProfile.copy(friendsInfo = updatedInfo)
                        selectedSection = null
                    }
                )
            }
            ProfileSection.PRIVATE -> {
                EditPrivateInfoDialog(
                    privateInfo = userProfile.privateInfo,
                    onDismiss = { selectedSection = null },
                    onSave = { updatedInfo ->
                        userProfile = userProfile.copy(privateInfo = updatedInfo)
                        selectedSection = null
                    }
                )
            }
            ProfileSection.MUSIC_PREFERENCES -> {
                MusicPreferencesDialog(
                    musicPreferences = userProfile.musicPreferences,
                    onDismiss = { selectedSection = null },
                    onSave = { updatedPreferences ->
                        userProfile = userProfile.copy(musicPreferences = updatedPreferences)
                        selectedSection = null
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    userProfile: UserProfile
) {    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = purpleGradient)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Profile Picture
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
            }
            
            Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = userProfile.publicInfo.displayName,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@${userProfile.publicInfo.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            if (userProfile.publicInfo.bio.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userProfile.publicInfo.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary            )
        }
    }
}