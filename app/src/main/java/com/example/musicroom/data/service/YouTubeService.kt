package com.example.musicroom.data.service

import com.example.musicroom.BuildConfig
import com.example.musicroom.data.api.YouTubeApiService
import com.example.musicroom.data.models.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeService @Inject constructor(
    private val apiService: YouTubeApiService
) {
    // Use BuildConfig for API key
    private val apiKey = BuildConfig.YOUTUBE_API_KEY
      suspend fun searchTracks(query: String): Result<List<Track>> {
        return try {
            // Log the API key for debugging (remove in production)
            println("DEBUG: Using API key: ${apiKey.take(10)}...")
            
            val response = apiService.searchVideos(
                query = "$query music",
                apiKey = apiKey
            )
            
            val tracks = response.items.map { video ->
                Track(
                    id = video.id.videoId,
                    title = video.snippet.title,
                    artist = extractArtistFromTitle(video.snippet.title, video.snippet.channelTitle),
                    thumbnailUrl = video.snippet.thumbnails.high.url,
                    youtubeUrl = "https://www.youtube.com/watch?v=${video.id.videoId}",
                    duration = "Unknown", // YouTube Data API v3 doesn't provide duration in search
                    channelTitle = video.snippet.channelTitle,
                    description = video.snippet.description
                )
            }
            
            Result.success(tracks)
        } catch (e: Exception) {
            // Enhanced error logging
            println("DEBUG: YouTube API Error - ${e.message}")
            println("DEBUG: Error type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    private fun extractArtistFromTitle(title: String, channelTitle: String): String {
        // Try to extract artist from title (common patterns: "Artist - Song", "Song by Artist")
        val patterns = listOf(
            Regex("^([^-]+)\\s*-\\s*(.+)$"), // "Artist - Song"
            Regex("^(.+)\\s*by\\s*([^(]+)"), // "Song by Artist"
            Regex("^([^|]+)\\s*\\|\\s*(.+)$") // "Artist | Song"
        )
        
        for (pattern in patterns) {
            val match = pattern.find(title)
            if (match != null) {
                val firstGroup = match.groupValues[1].trim()
                val secondGroup = match.groupValues[2].trim()
                
                // Check which group looks more like an artist name (shorter, less special chars)
                return if (firstGroup.length < secondGroup.length && 
                          !firstGroup.contains("(") && !firstGroup.contains("[")) {
                    firstGroup
                } else {
                    secondGroup
                }
            }
        }
        
        // Fallback to channel title if no pattern matches
        return channelTitle.replace("VEVO", "").replace("Records", "").trim()
    }
    
    fun formatDuration(durationString: String): String {
        // YouTube duration format: PT4M20S -> 4:20
        // For now, return as-is since we don't get duration from search API
        return "Unknown"
    }
}
