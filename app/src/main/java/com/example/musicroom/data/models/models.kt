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
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val imageUrl: String? = null,
    var votes: Int = 0,
    val addedBy: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing a music room
 *
 * @property id Unique identifier for the room
 * @property name Name of the room
 * @property hostId ID of the host user
 * @property isPublic Whether the room is public
 * @property licenseType Type of license for the room
 * @property participants List of participant user IDs
 * @property playlist List of tracks in the room's playlist
 * @property connectedDevices List of devices connected to the room
 */
data class Room(
    val id: String = "",
    val name: String = "",
    val hostId: String = "",
    val isPublic: Boolean = true,
    val licenseType: LicenseType = LicenseType.OpenVote,
    val participants: List<String> = emptyList(),
    val playlist: List<Track> = emptyList(),
    val connectedDevices: List<Device> = emptyList()
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
