package com.example.musicroom.data.models

data class MusicRoom(
    val id: String,
    val name: String,
    val hostName: String,
    val currentTrack: String,
    val currentArtist: String,
    val listeners: Int,
    val isLive: Boolean,
    val playlist: List<Track> = emptyList()
)

// Sample data
val activeRooms = listOf(
    MusicRoom(
        id = "1",
        name = "LFERDA's Room",
        hostName = "LFERDA",
        currentTrack = "New Track",
        currentArtist = "Figoshin",
        listeners = 42,
        isLive = true,
        playlist = listOf(
            Track("1", "Track 1", "Artist 1", votes = 42),
            Track("2", "Track 2", "Artist 2", votes = 35)
        )
    ),
    MusicRoom(
        id = "2",
        name = "Morning Beats",
        hostName = "Cheb Mami",
        currentTrack = "Morning Vibes",
        currentArtist = "LFERDA",
        listeners = 28,
        isLive = false,
        playlist = listOf(
            Track("3", "Track 3", "Artist 3", votes = 28),
            Track("4", "Track 4", "Artist 4", votes = 22)
        )
    ),
    MusicRoom(
        id = "3",
        name = "Evening Chillout",
        hostName = "Figoshin",
        currentTrack = "Evening Relax",
        currentArtist = "Cheb Mami",
        listeners = 15,
        isLive = true,
        playlist = listOf(
            Track("5", "Track 5", "Artist 5", votes = 15),
            Track("6", "Track 6", "Artist 6", votes = 10)
        )
    )
) 