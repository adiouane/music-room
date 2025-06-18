package com.example.musicroom.data.models

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val youtubeUrl: String,
    val duration: String,
    val channelTitle: String = "",
    val description: String = ""
)

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

// YouTube API response models
data class YouTubeSearchResponse(
    val items: List<YouTubeVideo>
)

data class YouTubeVideo(
    val id: YouTubeVideoId,
    val snippet: YouTubeSnippet
)

data class YouTubeVideoId(
    val videoId: String
)

data class YouTubeSnippet(
    val title: String,
    val description: String,
    val channelTitle: String,
    val thumbnails: YouTubeThumbnails
)

data class YouTubeThumbnails(
    val high: YouTubeThumbnail
)

data class YouTubeThumbnail(
    val url: String
)
