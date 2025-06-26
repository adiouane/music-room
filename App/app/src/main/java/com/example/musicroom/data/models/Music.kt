/**
 * ========================================================================================
 * MUSIC DATA MODELS
 * ========================================================================================
 * 
 * Core data classes for music-related entities in the app.
 * Simplified and cleaned after removing YouTube API and Supabase dependencies.
 * 
 * 🎵 TRACK MODEL:
 * ========================================================================================
 * Represents a single music track with essential metadata.
 * Used throughout the app for search results, playlists, and player.
 * 
 * 📝 PLAYLIST MODEL:
 * ========================================================================================
 * Represents a music playlist containing multiple tracks.
 * Supports both public and private playlists with user ownership.
 * 
 * 🔄 FOR DEVELOPERS - BACKEND INTEGRATION:
 * ========================================================================================
 * These models are ready for API integration:
 * 1. Add @Serializable annotation if using Kotlin Serialization
 * 2. Add @SerializedName if using Gson/Retrofit
 * 3. Add validation annotations if needed (@NotNull, @Size, etc.)
 * 4. Consider adding more metadata fields as needed:
 *    - genre, album, releaseDate, popularity, etc.
 * 
 * 🧹 CLEANUP NOTES:
 * ========================================================================================
 * ✅ Removed youtubeUrl field from Track (YouTube API removed)
 * ✅ Removed YouTube-specific response models
 * ✅ Simplified to core music metadata only
 * ✅ Maintained compatibility with existing UI components
 * ========================================================================================
 */

package com.example.musicroom.data.models

/**
 * ============================================================================
 * TRACK DATA CLASS
 * ============================================================================
 * 
 * Represents a single music track with core metadata.
 * 
 * @param id Unique identifier for the track
 * @param title Track name/title
 * @param artist Artist or band name
 * @param thumbnailUrl URL to track artwork/thumbnail image
 * @param duration Track length in MM:SS format (e.g., "3:45")
 * @param channelTitle Optional: Channel/label name (default empty)
 * @param description Optional: Track description/details (default empty)
 * 
 * 💡 USAGE EXAMPLES:
 * - Music search results
 * - Playlist track lists  
 * - Now playing screen
 * - Track sharing and favorites
 * ============================================================================
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: String,
    val channelTitle: String = "",
    val description: String = ""
)

/**
 * ============================================================================
 * PLAYLIST DATA CLASS
 * ============================================================================
 * 
 * Represents a music playlist containing multiple tracks.
 * 
 * @param id Unique identifier for the playlist
 * @param name Playlist title/name
 * @param description Optional: Playlist description (default empty)
 * @param isPublic Whether playlist is public or private (default true)
 * @param createdBy User ID of playlist creator
 * @param tracks List of tracks in the playlist (default empty)
 * @param createdAt Optional: Creation timestamp (default empty)
 * 
 * 💡 USAGE EXAMPLES:
 * - User-created playlists
 * - Shared/collaborative playlists
 * - Curated music collections
 * - Playlist browsing and discovery
 * ============================================================================
 */
data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val isPublic: Boolean = true,
    val createdBy: String,
    val tracks: List<Track> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)
