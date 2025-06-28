package com.example.musicroom.presentation.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musicroom.data.models.*
import com.example.musicroom.presentation.theme.*

enum class ProfileSection {
    PUBLIC_INFO,
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
                id = user.id,
                publicInfo = PublicInfo(
                    displayName = user.name,
                    username = user.username,
                    bio = "Music lover and playlist creator",
                    profilePictureUrl = user.photoUrl
                ),
                friendsInfo = FriendsInfo(
                    email = user.email,
                    realName = "John Doe",
                    location = "New York, USA"
                ),
                privateInfo = PrivateInfo(
                    phoneNumber = "+1 234 567 8900",
                    birthDate = "1990-01-01",
                    notes = "Personal notes..."
                ),
                musicPreferences = MusicPreferences(
                    favoriteGenres = listOf("Rock", "Jazz", "Electronic"),
                    favoriteArtists = listOf("The Beatles", "Miles Davis", "Daft Punk"),
                    musicMood = "Relaxed",
                    explicitContent = false
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = userProfile.publicInfo.profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(DarkSurface),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userProfile.publicInfo.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "@${userProfile.publicInfo.username}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )

                if (userProfile.publicInfo.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = userProfile.publicInfo.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Sections
            ProfileSectionCard(
                title = "Public Information",
                description = "Information visible to all users",
                icon = Icons.Default.Person,
                onClick = { selectedSection = ProfileSection.PUBLIC_INFO }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(
                title = "Friends Information",
                description = "Information visible to friends only",
                icon = Icons.Default.Group,
                onClick = { selectedSection = ProfileSection.FRIENDS_INFO }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(
                title = "Private Information",
                description = "Information visible only to you",
                icon = Icons.Default.Lock,
                onClick = { selectedSection = ProfileSection.PRIVATE }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileSectionCard(
                title = "Music Preferences",
                description = "Your music taste and preferences",
                icon = Icons.Default.MusicNote,
                onClick = { selectedSection = ProfileSection.MUSIC_PREFERENCES }
            )
        }
    }

    // Handle dialog opening
    when (selectedSection) {
        ProfileSection.PUBLIC_INFO -> {
            EditPublicInfoDialog(
                publicInfo = userProfile.publicInfo,
                onDismiss = { selectedSection = null },
                onSave = { newPublicInfo ->
                    userProfile = userProfile.copy(publicInfo = newPublicInfo)
                    selectedSection = null
                }
            )
        }
        ProfileSection.FRIENDS_INFO -> {
            EditFriendsInfoDialog(
                friendsInfo = userProfile.friendsInfo,
                onDismiss = { selectedSection = null },
                onSave = { newFriendsInfo ->
                    userProfile = userProfile.copy(friendsInfo = newFriendsInfo)
                    selectedSection = null
                }
            )
        }
        ProfileSection.PRIVATE -> {
            EditPrivateInfoDialog(
                privateInfo = userProfile.privateInfo,
                onDismiss = { selectedSection = null },
                onSave = { newPrivateInfo ->
                    userProfile = userProfile.copy(privateInfo = newPrivateInfo)
                    selectedSection = null
                }
            )
        }
        ProfileSection.MUSIC_PREFERENCES -> {
            MusicPreferencesDialog(
                musicPreferences = userProfile.musicPreferences,
                onDismiss = { selectedSection = null },
                onSave = { newMusicPreferences ->
                    userProfile = userProfile.copy(musicPreferences = newMusicPreferences)
                    selectedSection = null
                }
            )
        }
        null -> {
            // No dialog open
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
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

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = TextSecondary
            )
        }
    }
}

// Mock data helper function
fun createMockUserProfile(user: User): UserProfile {
    return UserProfile(
        id = user.id,
        publicInfo = PublicInfo(
            displayName = user.name,
            username = user.username,
            bio = "Music enthusiast and playlist creator. Love discovering new artists and sharing great music with friends.",
            profilePictureUrl = user.photoUrl
        ),
        friendsInfo = FriendsInfo(
            email = user.email,
            realName = "John Doe",
            location = "San Francisco, CA"
        ),
        privateInfo = PrivateInfo(
            phoneNumber = "+1 (555) 123-4567",
            birthDate = "January 15, 1995",
            notes = "Remember to update playlist for workout sessions"
        ),
        musicPreferences = MusicPreferences(
            favoriteGenres = listOf("Electronic", "Indie Rock", "Jazz", "Hip Hop"),
            favoriteArtists = listOf("Radiohead", "Daft Punk", "Billie Eilish", "John Coltrane"),
            musicMood = "Energetic",
            explicitContent = true
        )
    )
}