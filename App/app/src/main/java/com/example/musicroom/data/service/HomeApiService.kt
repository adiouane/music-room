package com.example.musicroom.data.service

import android.util.Log
import com.example.musicroom.data.auth.TokenManager
import com.example.musicroom.data.models.HomeResponse
import com.example.musicroom.data.network.NetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection

/**
 * API Service for home screen data
 * Fetches user playlists, recommended songs, popular content, etc.
 */
@Singleton
class HomeApiService @Inject constructor(
    private val tokenManager: TokenManager
) {
    
    /**
     * Fetch home screen data from backend
     */
    suspend fun getHomeData(): Result<HomeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("HomeAPI", "🏠 Fetching home data")
                Log.d("HomeAPI", "🌐 Using base URL: ${NetworkConfig.getCurrentBaseUrl()}")
                
                val fullUrl = NetworkConfig.getFullUrl(NetworkConfig.Endpoints.HOME)
                Log.d("HomeAPI", "📡 Full URL: $fullUrl")
                
                val url = URL(fullUrl)
                val connection = url.openConnection() as HttpURLConnection
                
                // Handle HTTPS for Codespaces
                if (connection is HttpsURLConnection) {
                    Log.d("HomeAPI", "🔒 Using HTTPS connection")
                }
                
                connection.apply {
                    requestMethod = "GET"
                    doInput = true
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "MusicRoom-Android-App")
                    
                    // Add authentication token if available
                    tokenManager.getToken()?.let { token ->
                        setRequestProperty("Authorization", "Bearer $token")
                        Log.d("HomeAPI", "🔑 Added auth token to request")
                    }
                    
                    // Add CORS headers for Codespaces
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                val responseCode = connection.responseCode
                Log.d("HomeAPI", "📨 Response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("HomeAPI", "📋 Response received (${responseText.length} characters)")
                
                when (responseCode) {
                    200 -> {
                        try {
                            // Parse the JSON response
                            val homeResponse = parseHomeResponse(responseText)
                            Log.d("HomeAPI", "✅ Home data parsed successfully")
                            Result.success(homeResponse)
                        } catch (e: Exception) {
                            Log.e("HomeAPI", "❌ Error parsing home response", e)
                            Result.failure(Exception("Error parsing home data: ${e.message}"))
                        }
                    }
                    401 -> {
                        Log.e("HomeAPI", "❌ Unauthorized - token may be invalid")
                        Result.failure(Exception("Authentication required"))
                    }
                    404 -> {
                        Log.e("HomeAPI", "❌ Home endpoint not found")
                        Result.failure(Exception("Home endpoint not found"))
                    }
                    else -> {
                        Log.e("HomeAPI", "❌ Server error: $responseCode")
                        Result.failure(Exception("Server error occurred (Code: $responseCode)"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("HomeAPI", "❌ Network error: ${e.message}", e)
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }
    
    /**
     * Parse the JSON response into HomeResponse object
     * This is a simplified parsing - you might want to use a proper JSON library like Gson
     */
    private fun parseHomeResponse(jsonString: String): HomeResponse {
        val json = JSONObject(jsonString)
        
        // For now, return a simplified version with basic data
        // You can expand this to fully parse all the complex nested structures
        
        return HomeResponse(
            user_playlists = parsePlaylistsSection(json.optJSONObject("user_playlists")),
            recommended_songs = parseSongsSection(json.optJSONObject("recommended_songs")),
            popular_songs = parseSongsSection(json.optJSONObject("popular_songs")),
            recently_listened = parseSongsSection(json.optJSONObject("recently_listened")),
            popular_artists = parseArtistsSection(json.optJSONObject("popular_artists")),
            events = emptyList() // Parse events if needed
        )
    }
    
    private fun parsePlaylistsSection(json: JSONObject?): com.example.musicroom.data.models.PlaylistsSection {
        if (json == null) {
            return com.example.musicroom.data.models.PlaylistsSection(
                headers = com.example.musicroom.data.models.ApiHeaders("error", -1, "No data", "", 0, null),
                results = emptyList()
            )
        }
        
        val headers = parseHeaders(json.optJSONObject("headers"))
        val resultsArray = json.optJSONArray("results")
        val playlists = mutableListOf<com.example.musicroom.data.models.Playlist>()
        
        resultsArray?.let { array ->
            for (i in 0 until array.length()) {
                val playlistJson = array.optJSONObject(i)
                playlistJson?.let { 
                    playlists.add(
                        com.example.musicroom.data.models.Playlist(
                            id = it.optString("id"),
                            name = it.optString("name"),
                            creationdate = it.optString("creationdate"),
                            user_id = it.optString("user_id"),
                            user_name = it.optString("user_name"),
                            zip = it.optString("zip"),
                            shorturl = it.optString("shorturl"),
                            shareurl = it.optString("shareurl")
                        )
                    )
                }
            }
        }
        
        return com.example.musicroom.data.models.PlaylistsSection(headers, playlists)
    }
    
    private fun parseSongsSection(json: JSONObject?): com.example.musicroom.data.models.SongsSection {
        if (json == null) {
            return com.example.musicroom.data.models.SongsSection(
                headers = com.example.musicroom.data.models.ApiHeaders("error", -1, "No data", "", 0, null),
                results = emptyList()
            )
        }
        
        val headers = parseHeaders(json.optJSONObject("headers"))
        val resultsArray = json.optJSONArray("results")
        val songs = mutableListOf<com.example.musicroom.data.models.Song>()
        
        resultsArray?.let { array ->
            for (i in 0 until array.length()) {
                val songJson = array.optJSONObject(i)
                songJson?.let { 
                    songs.add(
                        com.example.musicroom.data.models.Song(
                            id = it.optString("id"),
                            name = it.optString("name"),
                            duration = it.optInt("duration"),
                            artist_id = it.optString("artist_id"),
                            artist_name = it.optString("artist_name"),
                            artist_idstr = it.optString("artist_idstr"),
                            album_name = it.optString("album_name"),
                            album_id = it.optString("album_id"),
                            license_ccurl = it.optString("license_ccurl"),
                            position = it.optInt("position"),
                            releasedate = it.optString("releasedate"),
                            album_image = it.optString("album_image"),
                            audio = it.optString("audio"),
                            audiodownload = it.optString("audiodownload"),
                            prourl = it.optString("prourl"),
                            shorturl = it.optString("shorturl"),
                            shareurl = it.optString("shareurl"),
                            waveform = it.optString("waveform"),
                            image = it.optString("image"),
                            audiodownload_allowed = it.optBoolean("audiodownload_allowed")
                        )
                    )
                }
            }
        }
        
        return com.example.musicroom.data.models.SongsSection(headers, songs)
    }
    
    private fun parseArtistsSection(json: JSONObject?): com.example.musicroom.data.models.ArtistsSection {
        if (json == null) {
            return com.example.musicroom.data.models.ArtistsSection(
                headers = com.example.musicroom.data.models.ApiHeaders("error", -1, "No data", "", 0, null),
                results = emptyList()
            )
        }
        
        val headers = parseHeaders(json.optJSONObject("headers"))
        val resultsArray = json.optJSONArray("results")
        val artists = mutableListOf<com.example.musicroom.data.models.Artist>()
        
        resultsArray?.let { array ->
            for (i in 0 until array.length()) {
                val artistJson = array.optJSONObject(i)
                artistJson?.let { 
                    artists.add(
                        com.example.musicroom.data.models.Artist(
                            id = it.optString("id"),
                            name = it.optString("name"),
                            website = it.optString("website"),
                            joindate = it.optString("joindate"),
                            image = it.optString("image"),
                            shorturl = it.optString("shorturl"),
                            shareurl = it.optString("shareurl")
                        )
                    )
                }
            }
        }
        
        return com.example.musicroom.data.models.ArtistsSection(headers, artists)
    }
    
    private fun parseHeaders(json: JSONObject?): com.example.musicroom.data.models.ApiHeaders {
        return if (json != null) {
            com.example.musicroom.data.models.ApiHeaders(
                status = json.optString("status"),
                code = json.optInt("code"),
                error_message = json.optString("error_message"),
                warnings = json.optString("warnings"),
                results_count = json.optInt("results_count"),
                next = json.optString("next")
            )
        } else {
            com.example.musicroom.data.models.ApiHeaders("error", -1, "No headers", "", 0, null)
        }
    }
}