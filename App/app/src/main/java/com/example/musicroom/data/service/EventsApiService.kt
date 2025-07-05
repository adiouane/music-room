package com.example.musicroom.data.service

import android.util.Log
import com.example.musicroom.data.auth.TokenManager
import com.example.musicroom.data.models.Event
import com.example.musicroom.data.models.EventOrganizer
import com.example.musicroom.data.models.Track
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

/**
 * API Service for Events operations
 * Handles event listing, creation, and management
 */
@Singleton
class EventsApiService @Inject constructor(
    private val tokenManager: TokenManager
) {
    
    /**
     * Get all public events
     */
    suspend fun getPublicEvents(location: String? = null): Result<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "📋 Fetching public events${location?.let { " for location: $it" } ?: ""}")
                
                val baseUrl = NetworkConfig.BASE_URL + "/api/events/"
                val urlWithParams = if (location != null) {
                    "$baseUrl?location=$location"
                } else {
                    baseUrl
                }
                
                val connection = (URL(urlWithParams).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()  // Fix: READ_TIMEOUT not Read_TIMEOUT
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Public events response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 Public events response: ${responseText.take(500)}...")
                
                when (responseCode) {
                    200 -> {
                        try {
                            val events = parsePublicEventsResponse(responseText)
                            Log.d("EventsAPI", "✅ Successfully parsed ${events.size} public events")
                            Result.success(events)
                        } catch (e: Exception) {
                            Log.e("EventsAPI", "❌ Error parsing public events response", e)
                            Result.failure(Exception("Failed to parse events: ${e.message}"))
                        }
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to fetch events: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error fetching public events", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get events where the user is involved (created, attending, etc.)
     */
    suspend fun getMyEvents(): Result<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "📋 Fetching my events")
                
                val url = "${NetworkConfig.BASE_URL}/api/events/my-events/"
                
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 My events response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 My events response: ${responseText.take(500)}...")
                
                when (responseCode) {
                    200 -> {
                        try {
                            val events = parseMyEventsResponse(responseText)
                            Log.d("EventsAPI", "✅ Successfully parsed ${events.size} my events")
                            Result.success(events)
                        } catch (e: Exception) {
                            Log.e("EventsAPI", "❌ Error parsing my events response", e)
                            Result.failure(Exception("Failed to parse my events: ${e.message}"))
                        }
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to fetch my events: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error fetching my events", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get events where the user can manage (owner/editor)
     * For adding tracks to events
     */
    suspend fun getUserManageableEvents(): Result<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "📋 Fetching manageable events")
                
                // Use the my-events endpoint and filter for manageable roles
                val myEventsResult = getMyEvents()
                if (myEventsResult.isFailure) {
                    return@withContext Result.failure(myEventsResult.exceptionOrNull() ?: Exception("Failed to get events"))
                }
                
                val allMyEvents = myEventsResult.getOrThrow()
                
                // Filter events where user has management permissions (owner or editor)
                val manageableEvents = allMyEvents.filter { event ->
                    event.current_user_role in listOf("owner", "editor")
                }
                
                Log.d("EventsAPI", "✅ Found ${manageableEvents.size} manageable events out of ${allMyEvents.size} total events")
                Result.success(manageableEvents)
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Error getting manageable events", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Create a new event
     */
    suspend fun createEvent(request: CreateEventRequest): Result<CreateEventResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "🎪 Creating new event: ${request.title}")
                
                val url = "${NetworkConfig.BASE_URL}/api/events/"
                val requestBody = JSONObject().apply {
                    put("title", request.title)
                    put("description", request.description ?: "")
                    put("location", request.location)
                    put("event_start_time", request.event_start_time)
                    put("event_end_time", request.event_end_time ?: "")
                    put("is_public", request.is_public)
                }.toString()
                
                Log.d("EventsAPI", "📤 Create event request: $requestBody")
                
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                // Write request body
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody)
                    writer.flush()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Create event response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 Create event response: $responseText")
                
                when (responseCode) {
                    201 -> {
                        try {
                            val jsonResponse = JSONObject(responseText)
                            val eventId = jsonResponse.optString("id")
                            val title = jsonResponse.optString("title")
                            
                            val response = CreateEventResponse(
                                success = true,
                                message = "Event created successfully",
                                eventId = eventId,
                                title = title
                            )
                            
                            Log.d("EventsAPI", "✅ Event created successfully: $eventId")
                            Result.success(response)
                        } catch (e: Exception) {
                            Log.e("EventsAPI", "❌ Error parsing create event response", e)
                            Result.failure(Exception("Failed to parse event creation response: ${e.message}"))
                        }
                    }
                    400 -> {
                        Log.e("EventsAPI", "❌ Bad request: $responseText")
                        val errorMessage = try {
                            val errorJson = JSONObject(responseText)
                            errorJson.optString("message", "Invalid event data")
                        } catch (e: Exception) {
                            "Invalid event data"
                        }
                        Result.failure(Exception(errorMessage))
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to create event: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error creating event", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Add a track to an event
     */
    suspend fun addTrackToEvent(eventId: String, trackId: String): Result<AddTrackToEventResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "🎵 Adding track $trackId to event $eventId")
                
                val url = "${NetworkConfig.BASE_URL}/api/events/$eventId/tracks/"
                val requestBody = JSONObject().apply {
                    put("track_id", trackId)
                }.toString()
                
                Log.d("EventsAPI", "📤 Add track request: $requestBody")
                
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                // Write request body
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody)
                    writer.flush()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Add track response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 Add track response: $responseText")
                
                when (responseCode) {
                    200, 201 -> {
                        try {
                            val response = AddTrackToEventResponse(
                                success = true,
                                message = "Track added successfully"
                            )
                            
                            Log.d("EventsAPI", "✅ Track added to event successfully")
                            Result.success(response)
                        } catch (e: Exception) {
                            Log.e("EventsAPI", "❌ Error parsing add track response", e)
                            Result.failure(Exception("Failed to parse add track response: ${e.message}"))
                        }
                    }
                    400 -> {
                        Log.e("EventsAPI", "❌ Bad request: $responseText")
                        Result.failure(Exception("Invalid track or event"))
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    403 -> {
                        Log.e("EventsAPI", "❌ Access denied")
                        Result.failure(Exception("You don't have permission to add tracks to this event"))
                    }
                    404 -> {
                        Log.e("EventsAPI", "❌ Event not found")
                        Result.failure(Exception("Event not found"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to add track: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error adding track to event", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get event details by ID
     */
    suspend fun getEventDetails(eventId: String): Result<Event> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "🎪 Fetching event details for ID: $eventId")
                
                val url = "${NetworkConfig.BASE_URL}/api/events/$eventId/"
                
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Event details response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 Event details response: $responseText")
                
                // Add specific logging before parsing
                Log.d("EventsAPI", "🔍 Raw response analysis:")
                val tempJson = JSONObject(responseText)
                Log.d("EventsAPI", "   - Raw current_user_role value: '${tempJson.opt("current_user_role")}'")
                Log.d("EventsAPI", "   - Is current_user_role null? ${tempJson.isNull("current_user_role")}")
                Log.d("EventsAPI", "   - user_roles object: '${tempJson.opt("user_roles")}'")
                Log.d("EventsAPI", "   - songs array: '${tempJson.opt("songs")}'")
                
                when (responseCode) {
                    200 -> {
                        try {
                            val event = parseEventDetailsResponse(responseText)
                            Log.d("EventsAPI", "✅ Successfully parsed event details: ${event.title}")
                            Log.d("EventsAPI", "   - Parsed current_user_role: '${event.current_user_role}'")
                            Result.success(event)
                        } catch (e: Exception) {
                            Log.e("EventsAPI", "❌ Error parsing event details response", e)
                            Result.failure(Exception("Failed to parse event details: ${e.message}"))
                        }
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    403 -> {
                        Log.e("EventsAPI", "❌ Access denied to event")
                        Result.failure(Exception("Access denied to this event"))
                    }
                    404 -> {
                        Log.e("EventsAPI", "❌ Event not found")
                        Result.failure(Exception("Event not found"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to fetch event details: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error fetching event details", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get tracks in an event with vote counts and full details
     */
    suspend fun getEventTracks(eventId: String): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "🎵 Fetching tracks for event ID: $eventId")
                
                val url = "${NetworkConfig.BASE_URL}/api/events/$eventId/tracks/"
                
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Event tracks response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 Event tracks response: $responseText")
                
                when (responseCode) {
                    200 -> {
                        try {
                            val tracks = parseEventTracksResponse(responseText)
                            Log.d("EventsAPI", "✅ Successfully parsed ${tracks.size} event tracks")
                            Result.success(tracks)
                        } catch (e: Exception) {
                            Log.e("EventsAPI", "❌ Error parsing event tracks response", e)
                            Result.failure(Exception("Failed to parse event tracks: ${e.message}"))
                        }
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    403 -> {
                        Log.e("EventsAPI", "❌ Access denied to event tracks")
                        Result.failure(Exception("Access denied to this event"))
                    }
                    404 -> {
                        Log.e("EventsAPI", "❌ Event not found")
                        Result.failure(Exception("Event not found"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to fetch event tracks: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error fetching event tracks", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get tracks in an event with vote counts and full details
     * This combines the track IDs from event details with full track information
     */
    suspend fun getEventTracksWithVotes(eventId: String): Result<List<Track>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "🎵 Fetching tracks with votes for event ID: $eventId")
                
                // Get tracks from the tracks endpoint
                val tracksResult = getEventTracks(eventId)
                if (tracksResult.isFailure) {
                    return@withContext Result.failure(tracksResult.exceptionOrNull() ?: Exception("Failed to get tracks"))
                }
                
                val tracks = tracksResult.getOrThrow()
                
                // Get vote information from event details
                val eventDetailsUrl = "${NetworkConfig.BASE_URL}/api/events/$eventId/"
                val voteData = getTrackVoteData(eventDetailsUrl)
                
                // Merge tracks with vote information and sort by vote count
                val tracksWithVotes = tracks.map { track ->
                    val voteInfo = voteData[track.id]
                    track.copy(
                        voteCount = voteInfo?.first ?: 0,
                        hasUserVoted = voteInfo?.second ?: false
                    )
                }.sortedByDescending { it.voteCount } // Sort by vote count (highest first)
                
                Log.d("EventsAPI", "✅ Successfully merged ${tracksWithVotes.size} tracks with votes")
                Result.success(tracksWithVotes)
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Error getting tracks with votes", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get vote data for tracks from event details response
     * Returns map of track_id to Pair(vote_count, user_has_voted)
     */
    private suspend fun getTrackVoteData(eventDetailsUrl: String): Map<String, Pair<Int, Boolean>> {
        return try {
            val connection = (URL(eventDetailsUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                
                val token = tokenManager.getToken()
                if (token != null) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
                
                if (NetworkConfig.isCodespaces()) {
                    setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                }
                
                connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
            }
            
            val responseText = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                reader.readText()
            }
            
            val eventJson = JSONObject(responseText)
            val songsArray = eventJson.optJSONArray("songs") ?: JSONArray()
            val voteData = mutableMapOf<String, Pair<Int, Boolean>>()
            
            for (i in 0 until songsArray.length()) {
                val songJson = songsArray.getJSONObject(i)
                val trackId = songJson.optString("track_id")
                val voteCount = songJson.optInt("vote_count", 0)
                // TODO: Add user vote status check when backend provides it
                val hasUserVoted = false // Placeholder until backend provides this info
                
                if (trackId.isNotBlank()) {
                    voteData[trackId] = Pair(voteCount, hasUserVoted)
                }
            }
            
            Log.d("EventsAPI", "🗳️ Extracted vote data for ${voteData.size} tracks")
            voteData
            
        } catch (e: Exception) {
            Log.e("EventsAPI", "❌ Error extracting vote data", e)
            emptyMap()
        }
    }
    
    /**
     * Vote for a track in an event
     */
    suspend fun voteForTrack(eventId: String, trackId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "👍 Voting for track $trackId in event $eventId")
                
                val url = "${NetworkConfig.BASE_URL}/api/events/$eventId/tracks/$trackId/vote/"
                
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Vote response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 Vote response: $responseText")
                
                when (responseCode) {
                    200, 201 -> {
                        val jsonResponse = JSONObject(responseText)
                        val message = jsonResponse.optString("message", "Vote added successfully")
                        Log.d("EventsAPI", "✅ Vote added successfully")
                        Result.success(message)
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    403 -> {
                        Log.e("EventsAPI", "❌ Access denied to vote")
                        Result.failure(Exception("Access denied"))
                    }
                    404 -> {
                        Log.e("EventsAPI", "❌ Event or track not found")
                        Result.failure(Exception("Event or track not found"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to vote: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error voting for track", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Remove vote for a track in an event
     */
    suspend fun unvoteForTrack(eventId: String, trackId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "👎 Removing vote for track $trackId in event $eventId")
                
                val url = "${NetworkConfig.BASE_URL}/api/events/$eventId/tracks/$trackId/unvote/"
                
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Unvote response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 Unvote response: $responseText")
                
                when (responseCode) {
                    200, 204 -> {
                        val message = try {
                            val jsonResponse = JSONObject(responseText)
                            jsonResponse.optString("message", "Vote removed successfully")
                        } catch (e: Exception) {
                            "Vote removed successfully"
                        }
                        Log.d("EventsAPI", "✅ Vote removed successfully")
                        Result.success(message)
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    403 -> {
                        Log.e("EventsAPI", "❌ Access denied to unvote")
                        Result.failure(Exception("Access denied"))
                    }
                    404 -> {
                        Log.e("EventsAPI", "❌ Event or track not found")
                        Result.failure(Exception("Event or track not found"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to remove vote: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error removing vote for track", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Join an event
     */
    suspend fun joinEvent(eventId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "🎪 Joining event ID: $eventId")
                
                val url = "${NetworkConfig.BASE_URL}/api/events/$eventId/join/"
                
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Join event response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 Join event response: $responseText")
                
                when (responseCode) {
                    200, 201 -> {
                        val jsonResponse = JSONObject(responseText)
                        val message = jsonResponse.optString("message", "Joined event successfully")
                        Log.d("EventsAPI", "✅ Joined event successfully")
                        Result.success(message)
                    }
                    400 -> {
                        Log.e("EventsAPI", "❌ Bad request: $responseText")
                        Result.failure(Exception("Unable to join event"))
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    403 -> {
                        Log.e("EventsAPI", "❌ Access denied to join event")
                        Result.failure(Exception("You cannot join this event"))
                    }
                    404 -> {
                        Log.e("EventsAPI", "❌ Event not found")
                        Result.failure(Exception("Event not found"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to join event: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error joining event", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Leave an event
     */
    suspend fun leaveEvent(eventId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "🚪 Leaving event ID: $eventId")
                
                val url = "${NetworkConfig.BASE_URL}/api/events/$eventId/leave/"
                
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    
                    // Add authorization header
                    val token = tokenManager.getToken()
                    if (token != null) {
                        setRequestProperty("Authorization", "Bearer $token")
                    }
                    
                    if (NetworkConfig.isCodespaces()) {
                        setRequestProperty("Origin", NetworkConfig.getCurrentBaseUrl())
                    }
                    
                    connectTimeout = NetworkConfig.Settings.CONNECT_TIMEOUT.toInt()
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Leave event response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📨 Leave event response: $responseText")
                
                when (responseCode) {
                    200, 204 -> {
                        val message = try {
                            val jsonResponse = JSONObject(responseText)
                            jsonResponse.optString("message", "Left event successfully")
                        } catch (e: Exception) {
                            "Left event successfully"
                        }
                        Log.d("EventsAPI", "✅ Left event successfully")
                        Result.success(message)
                    }
                    400 -> {
                        Log.e("EventsAPI", "❌ Bad request: $responseText")
                        Result.failure(Exception("Unable to leave event"))
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    403 -> {
                        Log.e("EventsAPI", "❌ Access denied to leave event")
                        Result.failure(Exception("You cannot leave this event"))
                    }
                    404 -> {
                        Log.e("EventsAPI", "❌ Event not found")
                        Result.failure(Exception("Event not found"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Error response: $responseText")
                        Result.failure(Exception("Failed to leave event: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error leaving event", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Parse public events response from JSON
     */
    private fun parsePublicEventsResponse(responseText: String): List<Event> {
        try {
            // First try to parse as JSONArray directly
            val eventsArray = try {
                JSONArray(responseText)
            } catch (e: Exception) {
                // If that fails, try to parse as JSONObject and extract array
                try {
                    val responseJson = JSONObject(responseText)
                    responseJson.optJSONArray("events") ?: responseJson.optJSONArray("results") ?: JSONArray()
                } catch (e2: Exception) {
                    Log.e("EventsAPI", "❌ Failed to parse response as both JSONArray and JSONObject", e2)
                    return emptyList()
                }
            }
            
            val events = mutableListOf<Event>()
            for (i in 0 until eventsArray.length()) {
                val eventJson = eventsArray.getJSONObject(i)
                
                // Parse organizer
                val organizerJson = eventJson.optJSONObject("organizer")
                val organizer = EventOrganizer(
                    id = organizerJson?.optString("id") ?: "",
                    name = organizerJson?.optString("name") ?: "Unknown",
                    avatar = organizerJson?.optString("avatar")
                )
                
                val event = Event(
                    id = eventJson.optString("id"),
                    title = eventJson.optString("title"),
                    description = eventJson.optString("description"),
                    location = eventJson.optString("location"),
                    organizer = organizer,
                    attendee_count = eventJson.optInt("attendee_count", 0),
                    track_count = eventJson.optInt("track_count", 0),
                    is_public = eventJson.optBoolean("is_public", true),
                    event_start_time = eventJson.optString("event_start_time"),
                    event_end_time = eventJson.optString("event_end_time"),
                    image_url = eventJson.optString("image_url"),
                    created_at = eventJson.optString("created_at"),
                    current_user_role = null // Public events don't have user role info
                )
                
                events.add(event)
                Log.d("EventsAPI", "🎪 Parsed public event: ${event.title}")
            }
            
            return events
            
        } catch (e: Exception) {
            Log.e("EventsAPI", "❌ Error parsing public events JSON", e)
            throw e
        }
    }
    
    /**
     * Parse my events response from JSON
     */
    private fun parseMyEventsResponse(responseText: String): List<Event> {
        try {
            val responseJson = JSONObject(responseText)
            val eventsArray = responseJson.optJSONArray("events") ?: responseJson.optJSONArray("results") ?: JSONArray()
            
            val events = mutableListOf<Event>()
            for (i in 0 until eventsArray.length()) {
                val eventJson = eventsArray.getJSONObject(i)
                
                // For "my events", the user_role field indicates the current user's role
                val userRole = eventJson.optString("user_role").takeIf { it.isNotBlank() }
                
                // Parse organizer - could be different from current user
                val organizerJson = eventJson.optJSONObject("organizer")
                val organizer = EventOrganizer(
                    id = organizerJson?.optString("id") ?: "",
                    name = organizerJson?.optString("name") ?: 
                          if (userRole == "owner") "You" else "Event Organizer", // Show "You" if current user is owner
                    avatar = organizerJson?.optString("avatar")
                )
                
                val event = Event(
                    id = eventJson.optString("id"),
                    title = eventJson.optString("title"),
                    description = eventJson.optString("description"),
                    location = eventJson.optString("location"),
                    organizer = organizer,
                    attendee_count = eventJson.optInt("attendee_count", 0),
                    track_count = eventJson.optInt("track_count", 0),
                    is_public = eventJson.optBoolean("is_public", true),
                    event_start_time = eventJson.optString("event_start_time"),
                    event_end_time = eventJson.optString("event_end_time"),
                    image_url = eventJson.optString("image_url"),
                    created_at = eventJson.optString("created_at"),
                    current_user_role = userRole // Map user_role to current_user_role
                )
                
                events.add(event)
                Log.d("EventsAPI", "🎪 Parsed my event: ${event.title} (role: $userRole)")
            }
            
            return events
            
        } catch (e: Exception) {
            Log.e("EventsAPI", "❌ Error parsing my events JSON", e)
            throw e
        }
    }
    
    /**
     * Parse single event details response from JSON
     */
    private fun parseEventDetailsResponse(responseText: String): Event {
        try {
            val eventJson = JSONObject(responseText)
            
            // Parse organizer
            val organizerJson = eventJson.optJSONObject("organizer")
            val organizer = EventOrganizer(
                id = organizerJson?.optString("id") ?: "",
                name = organizerJson?.optString("name") ?: "Unknown",
                avatar = organizerJson?.optString("avatar")
            )
            
            // Parse songs array to get track count
            val songsArray = eventJson.optJSONArray("songs")
            val trackCount = songsArray?.length() ?: eventJson.optInt("track_count", 0)
            
            // Handle current_user_role properly - check if it's actually null in JSON
            val currentUserRole = if (eventJson.isNull("current_user_role")) {
                null
            } else {
                eventJson.optString("current_user_role").takeIf { it.isNotBlank() }
            }
            
            Log.d("EventsAPI", "🔍 Parsing current_user_role:")
            Log.d("EventsAPI", "   - Raw JSON isNull: ${eventJson.isNull("current_user_role")}")
            Log.d("EventsAPI", "   - Raw JSON value: '${eventJson.opt("current_user_role")}'")
            Log.d("EventsAPI", "   - Parsed role: '$currentUserRole'")
            
            return Event(
                id = eventJson.optString("id"),
                title = eventJson.optString("title"),
                description = eventJson.optString("description"),
                location = eventJson.optString("location"),
                organizer = organizer,
                attendee_count = eventJson.optInt("attendee_count", 0),
                track_count = trackCount, // Use actual songs array length if available
                is_public = eventJson.optBoolean("is_public", true),
                event_start_time = eventJson.optString("event_start_time"),
                event_end_time = eventJson.optString("event_end_time"),
                image_url = eventJson.optString("image_url"),
                created_at = eventJson.optString("created_at"),
                current_user_role = currentUserRole
            )
            
        } catch (e: Exception) {
            Log.e("EventsAPI", "❌ Error parsing event details JSON", e)
            throw e
        }
    }
    
    /**
     * Parse event tracks response from JSON
     */
    private fun parseEventTracksResponse(responseText: String): List<Track> {
        try {
            val responseJson = JSONObject(responseText)
            val tracksArray = responseJson.optJSONArray("tracks") ?: JSONArray()
            
            val tracks = mutableListOf<Track>()
            for (i in 0 until tracksArray.length()) {
                val trackJson = tracksArray.getJSONObject(i)
                
                // Get artist name - could be in artist_name or nested artist object
                val artistName = trackJson.optString("artist_name").takeIf { it.isNotBlank() }
                    ?: trackJson.optJSONObject("artist")?.optString("name")
                    ?: "Unknown Artist"
                
                // Get duration - handle different formats
                val durationValue = trackJson.optString("duration", "0")
                val duration = when {
                    durationValue.contains(":") -> durationValue // Already formatted as MM:SS
                    durationValue.isNotBlank() && durationValue != "0" -> {
                        try {
                            val seconds = durationValue.toInt()
                            "${seconds / 60}:${String.format("%02d", seconds % 60)}"
                        } catch (e: Exception) {
                            durationValue
                        }
                    }
                    else -> "0:00"
                }
                
                // Get image URL - try different possible fields
                val imageUrl = trackJson.optString("image")
                    .takeIf { it.isNotBlank() }
                    ?: trackJson.optString("album_image")
                    ?: trackJson.optString("cover")
                    ?: ""
                
                // Get audio URL - this will be used as description for playback
                val audioUrl = trackJson.optString("audio")
                    ?: trackJson.optString("audiodownload")
                    ?: ""
                
                val track = Track(
                    id = trackJson.optString("id", ""),
                    title = trackJson.optString("name", trackJson.optString("title", "Unknown Track")),
                    artist = artistName,
                    thumbnailUrl = imageUrl,
                    duration = duration,
                    description = audioUrl // Store audio URL in description for playback
                )
                
                tracks.add(track)
                Log.d("EventsAPI", "🎵 Parsed track: ${track.title} by ${track.artist}")
            }
            
            return tracks
            
        } catch (e: Exception) {
            Log.e("EventsAPI", "❌ Error parsing event tracks JSON", e)
            throw e
        }
    }
}

/**
 * Data classes for Event API responses
 */
data class CreateEventResponse(
    val success: Boolean,
    val message: String,
    val eventId: String? = null,
    val title: String? = null
)

/**
 * Request data class for creating events
 */
data class CreateEventRequest(
    val title: String,
    val description: String? = null,
    val location: String,
    val event_start_time: String,
    val event_end_time: String? = null,
    val is_public: Boolean = true
)

/**
 * Response for adding track to event
 */
data class AddTrackToEventResponse(
    val success: Boolean,
    val message: String
)