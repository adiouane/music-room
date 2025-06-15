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
    val username: String,
    val photoUrl: String,
    val email: String = "",
)

/**
 * Data class representing a music track
 *
 * @property id Unique identifier for the track
 * @property title Title of the track
 * @property artist Artist name
 * @property imageUrl URL to the album art image
 * @property votes Number of votes the track has received
 * @property addedBy User ID of the person who added the track
 * @property timestamp Timestamp when the track was added
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String? = null,
    var votes: Int = 0,
    val addedBy: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing an artist
 *
 * @property id Unique identifier for the artist
 * @property name Artist name
 * @property image URL to the artist's image
 * @property website Artist's website URL
 * @property joindate Date when the artist joined the platform
 * @property shorturl Short URL to artist's profile
 * @property shareurl Share URL to artist's profile
 */
data class Artist(
    val id: String,
    val name: String,
    val image: String,
    val website: String,
    val joindate: String,
    val shorturl: String,
    val shareurl: String
)

/**
 * Data classes for API responses
 */
data class ArtistsApiResponse(
    val popular_artists: PopularArtists
)

data class PopularArtists(
    val headers: ApiHeaders,
    val results: List<Artist>
)

data class ApiHeaders(
    val status: String,
    val code: Int,
    val error_message: String,
    val warnings: String,
    val results_count: Int,
    val next: String?
)

/**
 * Data class representing a room in the application
 *
 * @property id Unique identifier for the room
 * @property name Name of the room
 * @property hostName Name of the host user
 * @property currentTrack Currently playing track title
 * @property currentArtist Currently playing artist name
 * @property listeners Number of listeners currently in the room
 * @property isLive Whether the room is currently live
 * @property playlist List of tracks in the room's playlist
 */
data class Room(
    val id: String,
    val name: String,
    val hostName: String,
    val currentTrack: String,
    val currentArtist: String,
    val listeners: Int,
    val isLive: Boolean,
    val playlist: List<Track> = emptyList()
)

/**
 * Data class representing a device connected to the room
 *
 * @property id Unique identifier for the device
 * @property name Name of the device
 * @property userId ID of the user associated with the device
 * @property permissions Permissions granted to the device
 */
data class Device(
    val id: String = "",
    val name: String = "",
    val userId: String = "",
    val permissions: DevicePermissions = DevicePermissions()
)

/**
 * Data class representing permissions for a device
 *
 * @property canPlay Whether the device can play music
 * @property canPause Whether the device can pause music
 * @property canSkip Whether the device can skip tracks
 * @property canAdjustVolume Whether the device can adjust volume
 */
data class DevicePermissions(
    val canPlay: Boolean = false,
    val canPause: Boolean = false,
    val canSkip: Boolean = false,
    val canAdjustVolume: Boolean = false
)

/**
 * Sealed class representing the license type for a room
 */
sealed class LicenseType {
    object OpenVote : LicenseType()
    object InviteOnly : LicenseType()
    data class GeoLocation(
        val latitude: Double,
        val longitude: Double,
        val radius: Int
    ) : LicenseType()
}
