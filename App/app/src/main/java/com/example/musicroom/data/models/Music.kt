/**
 * ========================================================================================
 * 🎵 MUSIC DOMAIN MODELS
 * ========================================================================================
 * 
 * Core data models for music-related entities in the MusicRoom app.
 * 
 * CHANGELOG:
 * ✅ Removed YouTube-specific response models
 * ✅ Simplified to core music metadata only
 * ✅ Maintained compatibility with existing UI components
 * ✅ Removed conflicting Playlist class (now in HomeModels.kt)
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

// Removed conflicting Playlist class - use the one in HomeModels.kt instead
