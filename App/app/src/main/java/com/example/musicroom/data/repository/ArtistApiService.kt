package com.example.musicroomi.data.repository

import com.example.musicroomi.data.models.ArtistsApiResponse
import retrofit2.http.GET

interface ArtistApiService {
    @GET("artists")
    suspend fun getPopularArtists(): ArtistsApiResponse
}