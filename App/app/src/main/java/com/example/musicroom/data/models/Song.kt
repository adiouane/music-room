package com.example.musicroom.data.models

data class Song(
    val id: String,
    val name: String,
    val duration: Int,
    val artistId: String,
    val artistName: String,
    val albumName: String,
    val albumId: String,
    val releasedate: String,
    val image: String,
    val audio: String,
    val audiodownload: String,
    val shorturl: String,
    val shareurl: String,
    val audiodownloadAllowed: Boolean
)