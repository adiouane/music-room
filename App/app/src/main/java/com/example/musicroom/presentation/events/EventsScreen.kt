package com.example.musicroom.presentation.events

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.musicroom.data.models.Event
import com.example.musicroom.presentation.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    navController: NavController,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val publicEventsState by viewModel.publicEventsState.collectAsState()
    val myEventsState by viewModel.myEventsState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val createResult by viewModel.createResult.collectAsState()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    
    // Handle create result
    LaunchedEffect(createResult) {
        createResult?.let { result ->
            when (result) {
                is CreateEventResult.Success -> {
                    // Show success for 2 seconds then clear
                    delay(2000)
                    viewModel.clearCreateResult()
                }
                is CreateEventResult.Error -> {
                    // Show error for 3 seconds then clear
                    delay(3000)
                    viewModel.clearCreateResult()
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Events",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Create Event Button
            IconButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Event",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Create result notification
        createResult?.let { result ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (result) {
                        is CreateEventResult.Success -> Color.Green.copy(alpha = 0.2f)
                        is CreateEventResult.Error -> Color.Red.copy(alpha = 0.2f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (result) {
                            is CreateEventResult.Success -> Icons.Default.CheckCircle
                            is CreateEventResult.Error -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = when (result) {
                            is CreateEventResult.Success -> Color.Green
                            is CreateEventResult.Error -> Color.Red
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (result) {
                            is CreateEventResult.Success -> "Event '${result.title}' created successfully!"
                            is CreateEventResult.Error -> "Error: ${result.message}"
                        },
                        color = when (result) {
                            is CreateEventResult.Success -> Color.Green
                            is CreateEventResult.Error -> Color.Red
                        },
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = DarkSurface,
            contentColor = TextPrimary,
            edgePadding = 16.dp
        ) {
            EventTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.switchTab(tab) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = when (tab) {
                                EventTab.PUBLIC -> Icons.Default.Public
                                EventTab.MY_EVENTS -> Icons.Default.Event
                            },
                            contentDescription = null,
                            tint = if (isSelected) PrimaryPurple else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tab.displayName,
                            color = if (isSelected) PrimaryPurple else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            val currentState = when (selectedTab) {
                EventTab.PUBLIC -> publicEventsState
                EventTab.MY_EVENTS -> myEventsState
            }
            
            EventsTabContent(
                uiState = currentState,
                selectedTab = selectedTab,
                onRefresh = { viewModel.refreshCurrentTab() },
                onEventClick = { event ->
                    try {
                        navController.navigate("event_details/${event.id}")
                    } catch (e: Exception) {
                        Log.e("EventsScreen", "Navigation error", e)
                    }
                }
            )
        }
    }
    
    // Create Event Dialog
    if (showCreateDialog) {
        CreateEventDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, description, location, startTime, isPublic ->
                viewModel.createEvent(title, description, location, startTime, isPublic)
                showCreateDialog = false
            },
            isCreating = isCreating
        )
    }
}

@Composable
private fun EventsTabContent(
    uiState: EventsUiState,
    selectedTab: EventTab,
    onRefresh: () -> Unit,
    onEventClick: (Event) -> Unit
) {
    when (uiState) {
        is EventsUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading ${selectedTab.displayName.lowercase()}...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        is EventsUiState.Success -> {
            if (uiState.events.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = when (selectedTab) {
                                EventTab.PUBLIC -> Icons.Default.Public
                                EventTab.MY_EVENTS -> Icons.Default.Event
                            },
                            contentDescription = "No events",
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (selectedTab) {
                                EventTab.PUBLIC -> "No public events"
                                EventTab.MY_EVENTS -> "No events yet"
                            },
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (selectedTab) {
                                EventTab.PUBLIC -> "Check back later for new events"
                                EventTab.MY_EVENTS -> "Create or join events to see them here"
                            },
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Events list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.events) { event ->
                        EventCard(
                            event = event,
                            selectedTab = selectedTab,
                            onClick = { onEventClick(event) }
                        )
                    }
                }
            }
        }
        
        is EventsUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error loading ${selectedTab.displayName.lowercase()}",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.message,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    selectedTab: EventTab,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.title,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Show role for my events, organizer for public events
                    when (selectedTab) {
                        EventTab.MY_EVENTS -> {
                            if (event.current_user_role != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (event.current_user_role) {
                                            "owner" -> Icons.Default.Star
                                            "editor" -> Icons.Default.Edit
                                            else -> Icons.Default.Person
                                        },
                                        contentDescription = "Role",
                                        tint = when (event.current_user_role) {
                                            "owner" -> Color(0xFFFFD700)
                                            "editor" -> PrimaryPurple
                                            else -> TextSecondary
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = event.current_user_role.replaceFirstChar { it.uppercase() },
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        EventTab.PUBLIC -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Organizer",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "by ${event.organizer.name}",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                // Public/Private badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (event.is_public) 
                            PrimaryPurple.copy(alpha = 0.2f) 
                        else 
                            Color.Red.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (event.is_public) "Public" else "Private",
                        color = if (event.is_public) PrimaryPurple else Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Event details
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.location,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Time",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatEventDateTime(event.event_start_time),
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Attendees",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.attendee_count} attending",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Tracks",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.track_count} tracks",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String, String?, Boolean) -> Unit,
    isCreating: Boolean
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Create Event",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = TextSecondary
                    ),
                    singleLine = true,
                    enabled = !isCreating
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Location field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = TextSecondary
                    ),
                    singleLine = true,
                    enabled = !isCreating
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = TextSecondary
                    ),
                    maxLines = 3,
                    enabled = !isCreating
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Public/Private toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Public Event",
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryPurple,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        enabled = !isCreating
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        ),
                        enabled = !isCreating
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (title.isNotBlank() && location.isNotBlank()) {
                                onConfirm(
                                    title.trim(),
                                    description.trim().takeIf { it.isNotBlank() },
                                    location.trim(),
                                    null, // Start time - using default in ViewModel
                                    isPublic
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        enabled = !isCreating && title.isNotBlank() && location.isNotBlank()
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

// Helper function to format date time
private fun formatEventDateTime(dateTimeString: String): String {
    // Simple formatting for now - you can improve this with proper date formatting
    return dateTimeString.take(19).replace("T", " at ")
}