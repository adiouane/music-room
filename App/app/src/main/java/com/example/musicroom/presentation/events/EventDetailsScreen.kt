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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

// UI State for Event Details
sealed class EventDetailsUiState {
    object Loading : EventDetailsUiState()
    data class Success(val event: Event, val tracks: List<Track>) : EventDetailsUiState()
    data class Error(val message: String) : EventDetailsUiState()
}

// ViewModel for Event Details
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
                
                val result = eventsApiService.getEventDetails(eventId)
                
                if (result.isSuccess) {
                    val eventDetails = result.getOrThrow()
                    Log.d("EventDetailsVM", "✅ Event details loaded: ${eventDetails.title}")
                    
                    // For now, convert songs array to Track objects (you might need to fetch full track details)
                    val tracks = emptyList<Track>() // TODO: Implement when you need to show tracks
                    
                    _uiState.value = EventDetailsUiState.Success(eventDetails, tracks)
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("EventDetailsVM", "❌ Failed to load event details", error)
                    _uiState.value = EventDetailsUiState.Error(error?.message ?: "Failed to load event details")
                }
                
            } catch (e: Exception) {
                Log.e("EventDetailsVM", "❌ Exception loading event details", e)
                _uiState.value = EventDetailsUiState.Error("Error loading event details: ${e.message}")
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
        
        // Tracks Section (for future implementation)
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
                            text = "${event.track_count} tracks",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (event.track_count == 0) {
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
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        // TODO: Show actual tracks when backend provides track details
                        Text(
                            text = "Track details coming soon...",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
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