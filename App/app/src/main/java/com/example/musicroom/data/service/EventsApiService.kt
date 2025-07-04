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
                    readTimeout = NetworkConfig.Settings.READ_TIMEOUT.toInt()
                }
                
                val responseCode = connection.responseCode
                Log.d("EventsAPI", "📨 Response code: $responseCode")
                
                val responseText = if (responseCode in 200..299) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                }
                
                Log.d("EventsAPI", "📋 Response received (${responseText.length} characters)")
                
                when (responseCode) {
                    200 -> {
                        try {
                            val events = parseEventsResponse(responseText)
                            Log.d("EventsAPI", "✅ Successfully parsed ${events.size} events")
                            Result.success(events)
                        } catch (e: Exception) {
                            Log.e("EventsAPI", "❌ Error parsing events response", e)
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
                Log.e("EventsAPI", "❌ Network error fetching events", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Create a new event
     */
    suspend fun createEvent(
        title: String,
        description: String?,
        location: String,
        eventStartTime: String,
        eventEndTime: String?,
        isPublic: Boolean = true
    ): Result<CreateEventResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("EventsAPI", "🆕 Creating event: $title at $location")
                
                val requestBody = JSONObject().apply {
                    put("title", title)
                    if (description != null) put("description", description)
                    put("location", location)
                    put("event_start_time", eventStartTime)
                    if (eventEndTime != null) put("event_end_time", eventEndTime)
                    put("is_public", isPublic)
                }
                
                Log.d("EventsAPI", "📤 Request body: $requestBody")
                
                val connection = (URL(NetworkConfig.BASE_URL + "/api/events/create/").openConnection() as HttpURLConnection).apply {
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
                    doOutput = true
                }
                
                // Send the request
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(requestBody.toString())
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
                            val response = CreateEventResponse(
                                success = true,
                                message = jsonResponse.optString("message", "Event created successfully"),
                                eventId = jsonResponse.optString("id"),
                                title = jsonResponse.optString("title", title)
                            )
                            Log.d("EventsAPI", "✅ Event created successfully: ${response.eventId}")
                            Result.success(response)
                        } catch (e: Exception) {
                            Log.e("EventsAPI", "❌ Error parsing create response", e)
                            Result.failure(Exception("Event created but failed to parse response"))
                        }
                    }
                    400 -> {
                        try {
                            val errorJson = JSONObject(responseText)
                            val errorMessage = errorJson.optString("error", "Invalid request")
                            Log.e("EventsAPI", "❌ Bad request: $errorMessage")
                            Result.failure(Exception(errorMessage))
                        } catch (e: Exception) {
                            Result.failure(Exception("Invalid request format"))
                        }
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Create event failed: $responseText")
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
                
                val url = "${NetworkConfig.BASE_URL}/api/events/$eventId/tracks/$trackId/add/"
                
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
                    doOutput = true
                }
                
                // Send empty body (as per your curl example)
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write("")
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
                            val jsonResponse = JSONObject(responseText)
                            val response = AddTrackToEventResponse(
                                success = true,
                                message = jsonResponse.optString("message", "Track added successfully")
                            )
                            Log.d("EventsAPI", "✅ Track added to event successfully")
                            Result.success(response)
                        } catch (e: Exception) {
                            Log.e("EventsAPI", "❌ Error parsing add track response", e)
                            Result.success(AddTrackToEventResponse(true, "Track added successfully"))
                        }
                    }
                    401 -> {
                        Log.e("EventsAPI", "❌ Unauthorized - token may be expired")
                        Result.failure(Exception("Authentication required"))
                    }
                    403 -> {
                        Log.e("EventsAPI", "❌ Forbidden - user may not have permission to add tracks to this event")
                        Result.failure(Exception("You don't have permission to add tracks to this event"))
                    }
                    404 -> {
                        Log.e("EventsAPI", "❌ Event or track not found")
                        Result.failure(Exception("Event or track not found"))
                    }
                    else -> {
                        Log.e("EventsAPI", "❌ Add track failed: $responseText")
                        Result.failure(Exception("Failed to add track to event: HTTP $responseCode"))
                    }
                }
                
            } catch (e: Exception) {
                Log.e("EventsAPI", "❌ Network error adding track to event", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get user's own events (events where user is organizer or has manage permissions)
     * For now, this filters public events by current user, but you could add a separate endpoint later
     */
    suspend fun getUserManageableEvents(): Result<List<Event>> {
        // TODO: Replace with dedicated endpoint like /api/events/my/ when backend provides it
        // For now, return all public events and let the user see which ones they can manage
        return getPublicEvents()
    }
    
    /**
     * Get user's events (events created by the user)
     */
    suspend fun getMyEvents(): Result<List<Event>> {
        // TODO: Implement when backend provides this endpoint
        // For now, filter public events by current user or implement separate endpoint
        return getPublicEvents()
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
                
                when (responseCode) {
                    200 -> {
                        try {
                            val event = parseEventDetailsResponse(responseText)
                            Log.d("EventsAPI", "✅ Successfully parsed event details: ${event.title}")
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
     * Parse events response - Updated to handle actual backend format
     */
    private fun parseEventsResponse(responseText: String): List<Event> {
        val events = mutableListOf<Event>()
        
        try {
            val jsonArray = JSONArray(responseText)
            
            for (i in 0 until jsonArray.length()) {
                val eventJson = jsonArray.getJSONObject(i)
                
                // Handle organizer - it could be a string or object
                val organizer = if (eventJson.has("organizer")) {
                    val organizerData = eventJson.get("organizer")
                    if (organizerData is String) {
                        // Backend returns organizer as string
                        EventOrganizer(
                            id = "", // Not provided in listing
                            name = organizerData,
                            avatar = null
                        )
                    } else {
                        // Backend returns organizer as object
                        val organizerJson = eventJson.getJSONObject("organizer")
                        EventOrganizer(
                            id = organizerJson.optString("id", ""),
                            name = organizerJson.optString("name", "Unknown"),
                            avatar = organizerJson.optString("avatar")
                        )
                    }
                } else {
                    EventOrganizer(id = "", name = "Unknown", avatar = null)
                }
                
                val event = Event(
                    id = eventJson.optString("id"),
                    title = eventJson.optString("title"),
                    description = eventJson.optString("description"), // May be null in listing
                    location = eventJson.optString("location"),
                    organizer = organizer,
                    attendee_count = eventJson.optInt("attendee_count", 0),
                    track_count = 0, // Not provided in listing for performance
                    is_public = eventJson.optBoolean("is_public", true),
                    event_start_time = eventJson.optString("event_start_time"),
                    event_end_time = eventJson.optString("event_end_time"), // May be null
                    image_url = eventJson.optString("image_url"), // May be null
                    created_at = eventJson.optString("created_at"), // May be null
                    current_user_role = eventJson.optString("current_user_role") // May be null in listing
                )
                
                events.add(event)
                Log.d("EventsAPI", "✅ Parsed event: ${event.title} by ${event.organizer.name}")
            }
            
        } catch (e: Exception) {
            Log.e("EventsAPI", "❌ Error parsing events JSON", e)
            throw e
        }
        
        return events
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
                current_user_role = eventJson.optString("current_user_role")
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