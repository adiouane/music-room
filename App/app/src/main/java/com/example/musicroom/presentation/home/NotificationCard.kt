package com.example.musicroom.presentation.home

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.musicroom.data.service.EventNotification
import com.example.musicroom.data.service.NotificationService
import com.example.musicroom.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * UI States for notifications
 */
sealed class NotificationsUiState {
    object Loading : NotificationsUiState()
    data class Success(val notifications: List<EventNotification>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationService: NotificationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    
    private val _actionInProgress = MutableStateFlow(false)
    val actionInProgress: StateFlow<Boolean> = _actionInProgress.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    fun loadNotifications() {
        viewModelScope.launch {
            try {
                _uiState.value = NotificationsUiState.Loading
                Log.d("NotificationsVM", "🔔 Loading event notifications")
                
                val result = notificationService.getEventNotifications()
                if (result.isSuccess) {
                    val notifications = result.getOrThrow()
                    Log.d("NotificationsVM", "✅ Loaded ${notifications.size} notifications")
                    _uiState.value = NotificationsUiState.Success(notifications)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    Log.e("NotificationsVM", "❌ Failed to load notifications: $error")
                    _uiState.value = NotificationsUiState.Error("Failed to load notifications: $error")
                }
                
            } catch (e: Exception) {
                Log.e("NotificationsVM", "❌ Error loading notifications", e)
                _uiState.value = NotificationsUiState.Error("Failed to load notifications: ${e.message}")
            }
        }
    }
    
    fun acceptInvitation(eventId: String) {
        viewModelScope.launch {
            try {
                _actionInProgress.value = true
                Log.d("NotificationsVM", "✅ Accepting invitation for event $eventId")
                
                val result = notificationService.acceptEventInvitation(eventId)
                if (result.isSuccess) {
                    Log.d("NotificationsVM", "✅ Successfully accepted invitation")
                    // Reload notifications to update the list
                    loadNotifications()
                } else {
                    Log.e("NotificationsVM", "❌ Failed to accept invitation: ${result.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                Log.e("NotificationsVM", "❌ Error accepting invitation", e)
            } finally {
                _actionInProgress.value = false
            }
        }
    }
    
    fun declineInvitation(eventId: String) {
        viewModelScope.launch {
            try {
                _actionInProgress.value = true
                Log.d("NotificationsVM", "❌ Declining invitation for event $eventId")
                
                val result = notificationService.declineEventInvitation(eventId)
                if (result.isSuccess) {
                    Log.d("NotificationsVM", "✅ Successfully declined invitation")
                    // Reload notifications to update the list
                    loadNotifications()
                } else {
                    Log.e("NotificationsVM", "❌ Failed to decline invitation: ${result.exceptionOrNull()?.message}")
                }
                
            } catch (e: Exception) {
                Log.e("NotificationsVM", "❌ Error declining invitation", e)
            } finally {
                _actionInProgress.value = false
            }
        }
    }
    
    fun refresh() {
        loadNotifications()
    }
}

@Composable
fun NotificationCard(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Event Invitations",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = { viewModel.refresh() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            when (val currentState = uiState) {
                is NotificationsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                is NotificationsUiState.Success -> {
                    if (currentState.notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "No notifications",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No pending invitations",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentState.notifications.take(3).forEach { notification ->
                                NotificationItem(
                                    notification = notification,
                                    actionInProgress = actionInProgress,
                                    onAccept = { viewModel.acceptInvitation(notification.eventId) },
                                    onDecline = { viewModel.declineInvitation(notification.eventId) },
                                    onEventClick = { 
                                        navController.navigate("event_details/${notification.eventId}")
                                    }
                                )
                            }
                            
                            if (currentState.notifications.size > 3) {
                                Text(
                                    text = "... and ${currentState.notifications.size - 3} more",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
                
                is NotificationsUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Failed to load notifications",
                                color = Color.Red,
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
private fun NotificationItem(
    notification: EventNotification,
    actionInProgress: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onEventClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBackground),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Event info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Event",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.eventTitle,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Invited by ${notification.inviterName}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = formatTimestamp(notification.timestamp),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Decline button
                OutlinedButton(
                    onClick = onDecline,
                    enabled = !actionInProgress,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    if (actionInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.Red,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Decline",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decline", fontSize = 12.sp)
                    }
                }
                
                // Accept button
                Button(
                    onClick = onAccept,
                    enabled = !actionInProgress,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    if (actionInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Accept",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept", fontSize = 12.sp)
                    }
                }
                
                // View event button
                OutlinedButton(
                    onClick = onEventClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryPurple
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "View",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}