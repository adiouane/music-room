package com.example.musicroom.presentation.events

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicroom.data.models.Event
import com.example.musicroom.data.models.Track
import com.example.musicroom.data.service.EventsApiService
import com.example.musicroom.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

// UI State for Event Details - Updated
sealed class EventDetailsUiState {
    object Loading : EventDetailsUiState()
    data class Success(
        val event: Event, 
        val tracks: List<Track>,
        val isAttending: Boolean = false,
        val isJoining: Boolean = false  // Add joining state
    ) : EventDetailsUiState()
    data class Error(val message: String) : EventDetailsUiState()
}

// ViewModel for Event Details - Updated
@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val eventsApiService: EventsApiService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<EventDetailsUiState>(EventDetailsUiState.Loading)
    val uiState: StateFlow<EventDetailsUiState> = _uiState.asStateFlow()
    
    fun loadEventDetails(eventId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = EventDetailsUiState.Loading
                Log.d("EventDetailsVM", "🎪 Loading event details for ID: $eventId")
                
                // Load event details first
                val eventResult = eventsApiService.getEventDetails(eventId)
                
                if (eventResult.isSuccess) {
                    val eventDetails = eventResult.getOrThrow()
                    
                    // Check if user is attending (organizer, attendee, or has any role)
                    val isAttending = eventDetails.current_user_role != null
                    
                    // Add explicit debug logging
                    Log.d("EventDetailsVM", "🔍 Event Details Analysis:")
                    Log.d("EventDetailsVM", "   - Event ID: ${eventDetails.id}")
                    Log.d("EventDetailsVM", "   - Event Title: ${eventDetails.title}")
                    Log.d("EventDetailsVM", "   - current_user_role: '${eventDetails.current_user_role}' (type: ${eventDetails.current_user_role?.javaClass?.simpleName ?: "null"})")
                    Log.d("EventDetailsVM", "   - isAttending calculation: ${eventDetails.current_user_role} != null = $isAttending")
                    Log.d("EventDetailsVM", "   - Expected behavior:")
                    if (isAttending) {
                        Log.d("EventDetailsVM", "     -> Should show LEAVE button and LOAD tracks")
                    } else {
                        Log.d("EventDetailsVM", "     -> Should show JOIN button and NOT load tracks")
                    }
                    
                    if (isAttending) {
                        // Load tracks only if attending
                        val tracksResult = eventsApiService.getEventTracks(eventId)
                        val tracks = if (tracksResult.isSuccess) {
                            tracksResult.getOrThrow()
                        } else {
                            Log.w("EventDetailsVM", "⚠️ Failed to load tracks: ${tracksResult.exceptionOrNull()?.message}")
                            emptyList()
                        }
                        
                        Log.d("EventDetailsVM", "✅ Event details loaded: ${eventDetails.title} with ${tracks.size} tracks (ATTENDING)")
                        _uiState.value = EventDetailsUiState.Success(eventDetails, tracks, true)
                    } else {
                        // User not attending, don't load tracks
                        Log.d("EventDetailsVM", "✅ Event details loaded: ${eventDetails.title} - User NOT attending")
                        _uiState.value = EventDetailsUiState.Success(eventDetails, emptyList(), false)
                    }
                } else {
                    val error = eventResult.exceptionOrNull()
                    Log.e("EventDetailsVM", "❌ Failed to load event details", error)
                    _uiState.value = EventDetailsUiState.Error(error?.message ?: "Failed to load event details")
                }
                
            } catch (e: Exception) {
                Log.e("EventDetailsVM", "❌ Exception loading event details", e)
                _uiState.value = EventDetailsUiState.Error("Error loading event details: ${e.message}")
            }
        }
    }
    
    fun joinEvent(eventId: String) {
        viewModelScope.launch {
            try {
                // Set joining state
                val currentState = _uiState.value
                if (currentState is EventDetailsUiState.Success) {
                    _uiState.value = currentState.copy(isJoining = true)
                }
                
                Log.d("EventDetailsVM", "🎪 Joining event: $eventId")
                val result = eventsApiService.joinEvent(eventId)
                
                if (result.isSuccess) {
                    Log.d("EventDetailsVM", "✅ Successfully joined event")
                    // Reload event details to get updated status and tracks
                    loadEventDetails(eventId)
                } else {
                    Log.e("EventDetailsVM", "❌ Failed to join event: ${result.exceptionOrNull()?.message}")
                    // Reset joining state on error
                    if (currentState is EventDetailsUiState.Success) {
                        _uiState.value = currentState.copy(isJoining = false)
                    }
                }
            } catch (e: Exception) {
                Log.e("EventDetailsVM", "❌ Exception joining event", e)
            }
        }
    }
    
    fun leaveEvent(eventId: String) {
        viewModelScope.launch {
            try {
                Log.d("EventDetailsVM", "🚪 Leaving event: $eventId")
                val result = eventsApiService.leaveEvent(eventId)
                
                if (result.isSuccess) {
                    Log.d("EventDetailsVM", "✅ Successfully left event")
                    // Reload event details to get updated status
                    loadEventDetails(eventId)
                } else {
                    Log.e("EventDetailsVM", "❌ Failed to leave event: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("EventDetailsVM", "❌ Exception leaving event", e)
            }
        }
    }
    
    fun refresh(eventId: String) {
        loadEventDetails(eventId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    navController: NavController,
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Load event details when screen opens
    LaunchedEffect(eventId) {
        viewModel.loadEventDetails(eventId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Event Details",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("EventDetailsScreen", "❌ Navigation error", e)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { 
                        try {
                            viewModel.refresh(eventId)
                        } catch (e: Exception) {
                            Log.e("EventDetailsScreen", "❌ Refresh error", e)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkSurface
            )
        )
        
        // Content
        when (val currentState = uiState) {
            is EventDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading event details...",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            is EventDetailsUiState.Success -> {
                EventDetailsContent(
                    event = currentState.event,
                    tracks = currentState.tracks,
                    isAttending = currentState.isAttending,
                    onJoinEvent = { viewModel.joinEvent(eventId) },
                    onLeaveEvent = { viewModel.leaveEvent(eventId) },
                    navController = navController
                )
            }
            
            is EventDetailsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "😔",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Failed to load event",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentState.message,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh(eventId) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDetailsContent(
    event: Event,
    tracks: List<Track>,
    isAttending: Boolean,
    onJoinEvent: () -> Unit,
    onLeaveEvent: () -> Unit,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Event Header
        item {
            EventHeaderCard(event = event)
        }
        
        // Event Info
        item {
            EventInfoCard(event = event)
        }
        
        // Join/Leave Event Button (for non-owners)
        if (event.current_user_role != "owner") { 
            item {
                // Add debug logging
                Log.d("EventDetailsScreen", "🔍 UI Button Logic:")
                Log.d("EventDetailsScreen", "   - event.current_user_role: '${event.current_user_role}'")
                Log.d("EventDetailsScreen", "   - isAttending: $isAttending")
                Log.d("EventDetailsScreen", "   - Should show: ${if (isAttending) "LEAVE (Red)" else "JOIN (Purple)"} button")
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isAttending) "You're attending this event" else "Join this event",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isAttending) "Access to tracks and event features" else "Join to access tracks and participate",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                        
                        Button(
                            onClick = if (isAttending) onLeaveEvent else onJoinEvent,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAttending) Color.Red else PrimaryPurple
                            )
                        ) {
                            Text(
                                text = if (isAttending) "Leave" else "Join",
                                color = Color.White
                            )
                            Log.d("EventDetailsScreen", "🔘 Button rendered: '${if (isAttending) "Leave" else "Join"}' (${if (isAttending) "Red" else "Purple"})")
                        }
                    }
                }
            }
        }
        
        // Event Management Options (for owners only)
        if (event.current_user_role == "owner") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Event Management",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "You are the organizer of this event",
                                color = Color(0xFFFFD700), // Gold color
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Owner",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Edit Event Button
                            OutlinedButton(
                                onClick = { 
                                    // TODO: Navigate to edit event screen
                                    Log.d("EventDetailsScreen", "🔧 Edit event clicked")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = PrimaryPurple
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit")
                            }
                            
                            // Delete Event Button
                            OutlinedButton(
                                onClick = { 
                                    // TODO: Show delete confirmation dialog
                                    Log.d("EventDetailsScreen", "🗑️ Delete event clicked")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
        
        // Tracks Section - Only show if attending or is owner
        if (isAttending || event.current_user_role == "owner") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Event Tracks",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tracks.size} tracks",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (tracks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "No tracks",
                                        modifier = Modifier.size(48.dp),
                                        tint = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No tracks yet",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Add songs to this event to see them here",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Display tracks list
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tracks.forEachIndexed { index, track ->
                                    EventTrackRow(
                                        track = track,
                                        position = index + 1,
                                        onTrackClick = {
                                            try {
                                                navigateToNowPlaying(navController, track)
                                            } catch (e: Exception) {
                                                Log.e("EventDetailsScreen", "❌ Navigation error", e)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Show message for non-attending users
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Join required",
                                modifier = Modifier.size(48.dp),
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Join event to access tracks",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "You need to join this event to see and play tracks",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventHeaderCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "by ${event.organizer.name}",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
                
                Icon(
                    imageVector = if (event.is_public) Icons.Default.Public else Icons.Default.Lock,
                    contentDescription = if (event.is_public) "Public event" else "Private event",
                    tint = if (event.is_public) Color.Green else Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (event.description?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = event.description,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun EventInfoCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Event Information",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Location",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = event.location,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Start Time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Start Time",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Start Time",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = event.event_start_time,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Attendees
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Attendees",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Attendees",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${event.attendee_count} attending",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Role
            if (event.current_user_role != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Your Role",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Your Role",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = event.current_user_role.replaceFirstChar { it.uppercase() },
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventTrackRow(
    track: Track,
    position: Int,
    onTrackClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position number
            Text(
                text = position.toString(),
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track artwork or music note icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                if (track.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(track.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Track artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Track info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Duration
            Text(
                text = track.duration,
                color = TextSecondary,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Play icon
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play track",
                tint = PrimaryPurple,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Helper function for navigation
private fun navigateToNowPlaying(navController: NavController, track: Track) {
    try {
        val encodedTitle = URLEncoder.encode(track.title, "UTF-8")
        val encodedArtist = URLEncoder.encode(track.artist, "UTF-8")
        val encodedDescription = URLEncoder.encode(track.description, "UTF-8")
        val encodedDuration = URLEncoder.encode(track.duration, "UTF-8")
        val encodedThumbnail = URLEncoder.encode(track.thumbnailUrl, "UTF-8")
        navController.navigate(
            "now_playing/${track.id}/$encodedTitle/$encodedArtist/$encodedThumbnail/$encodedDuration/$encodedDescription"
        )
    } catch (e: Exception) {
        Log.e("EventDetailsScreen", "❌ Navigation error: ${e.message}")
    }
}