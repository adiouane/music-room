package com.example.musicroom.data.sample

import com.example.musicroom.data.models.*

object SampleData {
    val rooms = listOf(
        Room(
            id = "1",
            name = "Chill Vibes",
            hostId = "user1",
            playlist = listOf(
                Track(
                    id = "track1",
                    title = "Blinding Lights",
                    artist = "The Weeknd",
                    votes = 5
                ),
                Track(
                    id = "track2", 
                    title = "As It Was",
                    artist = "Harry Styles",
                    votes = 3
                )
            )
        )
    )
}