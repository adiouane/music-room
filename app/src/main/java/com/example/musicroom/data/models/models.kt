package com.example.musicroom.data.models

/**
 * Data class representing a user in the application
 *
 * @property id Unique identifier for the user
 * @property name Display name of the user
 * @property photoUrl URL to the user's profile photo
 * @property email User's email address
 */
data class User(
    val id: String,
    val name: String,
    val photoUrl: String,
    val email: String = "",
)

/**
 * Data class representing a music track
 *
 * @property id Unique identifier for the track
 * @property title Title of the track
 * @property artist Artist name
 * @property albumArtUrl URL to the album art image
 * @property duration Duration of the track in seconds
 * @property tags List of tags associated with the track
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String,
    val duration: Int, // in seconds
    val tags: List<String> = emptyList()
)

/**
 * Data class representing a music room
 *
 * @property id Unique identifier for the room
 * @property name Name of the room
 * @property description Description of the room
 * @property coverUrl URL to the room cover image
 * @property memberCount Number of members in the room
 * @property tags List of tags associated with the room
 * @property isLive Whether the room is currently live
 */
data class Room(
    val id: String,
    val name: String,
    val description: String = "",
    val coverUrl: String = "",
    val memberCount: Int = 0,
    val tags: List<String> = emptyList(),
    val isLive: Boolean = true
)
