package com.example.musicroom.data.service

import android.util.Log
import com.example.musicroom.data.auth.TokenManager
import com.example.musicroom.data.network.NetworkConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection

/**
 * Enhanced data models for playlists - Updated to match your API response
 */
data class PublicPlaylist(
    val id: String,
    val name: String,
    val isPublic: Boolean,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val songCount: Int = 0,
    val followersCount: Int = 0,
    val description: String? = null,
    val isOwner: Boolean = false
)

data class CreatePlaylistRequest(
    val name: String,
    val isPublic: Boolean = true,
    val description: String? = null
)

data class CreatePlaylistResponse(
    val id: String,
    val name: String,
    val isPublic: Boolean,
    val createdAt: String? = null,
    val message: String? = null
)

/**
 * Enhanced API Service for playlist operations
 */
@Singleton
class PlaylistApiService @Inject constructor(
    private val tokenManager: TokenManager
) {
    
    /**
     * Get all public playlists
     */
    suspend fun getPublicPlaylists(): Result<List<PublicPlaylist>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("PlaylistAPI", "📋 Starting to fetch all public playlists")
                
                val fullUrl = NetworkConfig.BASE_URL + "/api/playlists/"
                Log.d("PlaylistAPI", "📡 Public URL: $fullUrl")
                
                val connection = createConnection(fullUrl, "GET")
                val responseCode = connection.responseCode
                val responseText = getResponseText(connection, responseCode)
                
                Log.d("PlaylistAPI", "📨 Public playlists response code: $responseCode")
                Log.d("PlaylistAPI", "📨 Public playlists response body: $responseText")
                
                when (responseCode) {
                    200 -> {
                        Log.d("PlaylistAPI", "✅ Public playlists successful response, parsing...")
                        val playlists = parsePlaylistsResponse(responseText, isOwner = false)
                        Log.d("PlaylistAPI", "✅ Successfully parsed ${playlists.size} public playlists")
                        Result.success(playlists)
                    }
                    401 -> {
                        Log.e("PlaylistAPI", "❌ Unauthorized for public playlists")
                        Result.failure(Exception("Authentication required. Please log in again."))
                    }
                    403 -> {
                        Log.e("PlaylistAPI", "❌ Forbidden")
                        Result.failure(Exception("Access denied. Check your permissions."))
                    }
                    404 -> {
                        Log.e("PlaylistAPI", "❌ Endpoint not found")
                        Result.failure(Exception("Playlists service is temporarily unavailable"))
                    }
                    500 -> {
                        Log.e("PlaylistAPI", "❌ Server error")
                        Result.failure(Exception("Server error. Please try again later."))
                    }
                    else -> {
                        Log.e("PlaylistAPI", "❌ Unexpected error: $responseCode - $responseText")
                        Result.failure(Exception("Failed to fetch public playlists (Error $responseCode)"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("PlaylistAPI", "❌ Network error fetching public playlists", e)
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }
    
    /**
     * Get user's own playlists - Enhanced with detailed logging
     */
    suspend fun getMyPlaylists(): Result<List<PublicPlaylist>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("PlaylistAPI", "📋 Starting to fetch user's own playlists")
                
                // Check if we have a token
                val token = tokenManager.getToken()
                if (token == null) {
                    Log.e("PlaylistAPI", "❌ No authentication token available")
                    return@withContext Result.failure(Exception("Authentication required. Please log in."))
                }
                Log.d("PlaylistAPI", "🔑 Using auth token: ${token.take(30)}...")
                
                val fullUrl = NetworkConfig.BASE_URL + "/api/playlists/my/"
                Log.d("PlaylistAPI", "📡 My playlists URL: $fullUrl")
                
                val connection = createConnection(fullUrl, "GET", requireAuth = true)
                
                Log.d("PlaylistAPI", "🌐 Making HTTP request to get my playlists...")
                val responseCode = connection.responseCode
                val responseText = getResponseText(connection, responseCode)
                
                Log.d("PlaylistAPI", "📨 My playlists response code: $responseCode")
                Log.d("PlaylistAPI", "📨 My playlists response body: $responseText")
                
                when (responseCode) {
                    200 -> {
                        Log.d("PlaylistAPI", "✅ My playlists successful response, parsing...")
                        val playlists = parsePlaylistsResponse(responseText, isOwner = true)
                        Log.d("PlaylistAPI", "✅ Successfully parsed ${playlists.size} user's playlists")
                        
                        // Log each playlist for debugging
                        playlists.forEachIndexed { index, playlist ->
                            Log.d("PlaylistAPI", "🎵 Playlist $index: ${playlist.name} (${if (playlist.isPublic) "public" else "private"}) - ${playlist.songCount} tracks")
                        }
                        
                        Result.success(playlists)
                    }
                    401 -> {
                        Log.e("PlaylistAPI", "❌ Unauthorized for my playlists - token may be expired")
                        Result.failure(Exception("Authentication required. Please log in again."))
                    }
                    403 -> {
                        Log.e("PlaylistAPI", "❌ Forbidden")
                        Result.failure(Exception("Access denied. Check your permissions."))
                    }
                    404 -> {
                        Log.e("PlaylistAPI", "❌ Endpoint not found")
                        Result.failure(Exception("My playlists service is temporarily unavailable"))
                    }
                    500 -> {
                        Log.e("PlaylistAPI", "❌ Server error")
                        Result.failure(Exception("Server error. Please try again later."))
                    }
                    else -> {
                        Log.e("PlaylistAPI", "❌ Unexpected error: $responseCode - $responseText")
                        Result.failure(Exception("Failed to fetch your playlists (Error $responseCode)"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("PlaylistAPI", "❌ Exception fetching my playlists", e)
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }
    
    /**
     * Create a new playlist
     */
    suspend fun createPlaylist(request: CreatePlaylistRequest): Result<CreatePlaylistResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate input
                if (request.name.isBlank()) {
                    return@withContext Result.failure(Exception("Playlist name cannot be empty"))
                }
                
                if (request.name.length > 100) {
                    return@withContext Result.failure(Exception("Playlist name is too long (max 100 characters)"))
                }
                
                Log.d("PlaylistAPI", "🆕 Creating playlist: ${request.name} (public: ${request.isPublic})")
                
                val token = tokenManager.getToken()
                if (token == null) {
                    Log.e("PlaylistAPI", "❌ No auth token for playlist creation")
                    return@withContext Result.failure(Exception("Authentication required. Please log in."))
                }
                
                val fullUrl = NetworkConfig.BASE_URL + "/api/playlists/create/"
                Log.d("PlaylistAPI", "📡 Create URL: $fullUrl")
                
                val connection = createConnection(fullUrl, "POST", requireAuth = true)
                
                // Create JSON body matching your API exactly
                val jsonBody = JSONObject().apply {
                    put("name", request.name.trim())
                    put("is_public", request.isPublic)
                    request.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            put("description", desc.trim())
                        }
                    }
                }
                
                Log.d("PlaylistAPI", "📤 Request body: $jsonBody")
                
                // Write JSON to request body
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonBody.toString())
                    writer.flush()
                }
                
                val responseCode = connection.responseCode
                val responseText = getResponseText(connection, responseCode)
                
                Log.d("PlaylistAPI", "📨 Create response code: $responseCode")
                Log.d("PlaylistAPI", "📨 Create response body: $responseText")
                
                when (responseCode) {
                    201 -> {
                        val response = parseCreatePlaylistResponse(responseText, request.name)
                        Log.d("PlaylistAPI", "✅ Created playlist: ${response.name}")
                        Result.success(response)
                    }
                    400 -> {
                        Log.e("PlaylistAPI", "❌ Bad request: $responseText")
                        val errorMessage = parseErrorMessage(responseText) ?: "Invalid playlist data"
                        Result.failure(Exception(errorMessage))
                    }
                    401 -> {
                        Log.e("PlaylistAPI", "❌ Unauthorized")
                        Result.failure(Exception("Authentication expired. Please log in again."))
                    }
                    409 -> {
                        Log.e("PlaylistAPI", "❌ Conflict: $responseText")
                        Result.failure(Exception("A playlist with this name already exists"))
                    }
                    422 -> {
                        Log.e("PlaylistAPI", "❌ Validation error: $responseText")
                        val errorMessage = parseErrorMessage(responseText) ?: "Invalid playlist data"
                        Result.failure(Exception(errorMessage))
                    }
                    500 -> {
                        Log.e("PlaylistAPI", "❌ Server error")
                        Result.failure(Exception("Server error. Please try again later."))
                    }
                    else -> {
                        Log.e("PlaylistAPI", "❌ Error: $responseCode - $responseText")
                        Result.failure(Exception("Failed to create playlist (Error $responseCode)"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("PlaylistAPI", "❌ Exception creating playlist", e)
                Result.failure(Exception("Network error: ${e.message}"))
            }
        }
    }
    
    /**
     * Helper function to create HTTP connection
     */
    private fun createConnection(url: String, method: String, requireAuth: Boolean = false): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        
        // Handle HTTPS for Codespaces
        if (connection is HttpsURLConnection) {
            Log.d("PlaylistAPI", "🔒 Using HTTPS connection")
        }
        
        connection.apply {
            requestMethod = method
            doInput = true
            if (method == "POST") doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "MusicRoom-Android-App")
            
            // Add authentication token
            val token = tokenManager.getToken()
            if (token != null) {
                setRequestProperty("Authorization", "Bearer $token")
                Log.d("PlaylistAPI", "🔑 Added auth token to request")
            } else if (requireAuth) {
                Log.w("PlaylistAPI", "⚠️ No auth token available for authenticated endpoint")
            }
            
            // Add CORS headers for Codespaces
            if (NetworkConfig.isCodespaces()) {
                setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
            }
            
            connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
            readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
        }
        
        return connection
    }
    
    /**
     * Helper function to get response text
     */
    private fun getResponseText(connection: HttpURLConnection, responseCode: Int): String {
        return if (responseCode in 200..299) {
            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                reader.readText()
            }
        } else {
            BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                reader.readText()
            }
        }
    }
    
    /**
     * Enhanced playlist response parsing - Updated to match your exact API response
     */
    private fun parsePlaylistsResponse(responseText: String, isOwner: Boolean): List<PublicPlaylist> {
        return try {
            Log.d("PlaylistAPI", "📝 Parsing playlists response...")
            Log.d("PlaylistAPI", "📄 Raw response: $responseText")
            
            val jsonArray = JSONArray(responseText)
            val playlists = mutableListOf<PublicPlaylist>()
            
            Log.d("PlaylistAPI", "📊 Found ${jsonArray.length()} playlists in response")
            
            for (i in 0 until jsonArray.length()) {
                val playlistJson = jsonArray.getJSONObject(i)
                
                // Log each playlist JSON for debugging
                Log.d("PlaylistAPI", "📋 Parsing playlist $i: $playlistJson")
                
                val id = playlistJson.optInt("id", 0).toString()
                val name = playlistJson.optString("name", "Untitled Playlist")
                val trackCount = playlistJson.optInt("track_count", 0)
                val followersCount = playlistJson.optInt("followers_count", 0)
                val isPublic = playlistJson.optBoolean("is_public", true)
                val createdAt = playlistJson.optString("created_at", null)
                
                val playlist = PublicPlaylist(
                    id = id,
                    name = name,
                    isPublic = isPublic,
                    createdBy = if (isOwner) "You" else null,
                    createdAt = createdAt,
                    updatedAt = null,
                    songCount = trackCount,
                    followersCount = followersCount,
                    description = playlistJson.optString("description", null),
                    isOwner = isOwner
                )
                
                Log.d("PlaylistAPI", "✅ Parsed playlist: ${playlist.name} (ID: ${playlist.id}, tracks: ${playlist.songCount}, public: ${playlist.isPublic})")
                playlists.add(playlist)
            }
            
            Log.d("PlaylistAPI", "📊 Successfully parsed ${playlists.size} playlists")
            playlists
        } catch (e: Exception) {
            Log.e("PlaylistAPI", "❌ Error parsing playlists response", e)
            Log.e("PlaylistAPI", "📄 Failed to parse response: $responseText")
            emptyList()
        }
    }
    
    /**
     * Enhanced create playlist response parsing
     */
    private fun parseCreatePlaylistResponse(responseText: String, fallbackName: String): CreatePlaylistResponse {
        return try {
            Log.d("PlaylistAPI", "📝 Parsing create response: $responseText")
            val json = JSONObject(responseText)
            
            val response = CreatePlaylistResponse(
                id = json.optInt("id", 0).toString(),
                name = json.optString("name", fallbackName),
                isPublic = json.optBoolean("is_public", true),
                createdAt = json.optString("created_at", null),
                message = "Playlist created successfully"
            )
            
            Log.d("PlaylistAPI", "✅ Parsed create response: ${response.name} (ID: ${response.id})")
            response
        } catch (e: Exception) {
            Log.e("PlaylistAPI", "❌ Error parsing create playlist response", e)
            Log.e("PlaylistAPI", "📄 Failed to parse response: $responseText")
            CreatePlaylistResponse(
                id = "",
                name = fallbackName,
                isPublic = true,
                message = "Playlist created successfully"
            )
        }
    }
    
    /**
     * Parse error messages from API responses
     */
    private fun parseErrorMessage(responseText: String): String? {
        return try {
            val json = JSONObject(responseText)
            when {
                json.has("error") -> json.getString("error")
                json.has("message") -> json.getString("message")
                json.has("detail") -> json.getString("detail")
                json.has("name") -> {
                    val nameErrors = json.getJSONArray("name")
                    if (nameErrors.length() > 0) {
                        nameErrors.getString(0)
                    } else null
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("PlaylistAPI", "Error parsing error message", e)
            null
        }
    }
}