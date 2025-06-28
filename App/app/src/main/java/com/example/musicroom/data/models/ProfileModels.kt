package com.example.musicroom.data.models

/**
 * Data models for user profile functionality
 */

/**
 * Main user profile data structure
 */
data class UserProfile(
    val id: String,
    val publicInfo: PublicInfo,
    val friendsInfo: FriendsInfo,
    val privateInfo: PrivateInfo,
    val musicPreferences: MusicPreferences
)

/**
 * Public profile information visible to all users
 */
data class PublicInfo(
    val displayName: String,
    val username: String,
    val bio: String,
    val profilePictureUrl: String = ""
)

/**
 * Profile information visible to friends only
 */
data class FriendsInfo(
    val email: String,
    val realName: String,
    val location: String
)

/**
 * Private profile information visible only to the user
 */
data class PrivateInfo(
    val phoneNumber: String,
    val birthDate: String,
    val notes: String
)

/**
 * User's music preferences and settings
 */
data class MusicPreferences(
    val favoriteGenres: List<String>,
    val favoriteArtists: List<String>,
    val musicMood: String,
    val explicitContent: Boolean
)