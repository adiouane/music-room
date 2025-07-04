package com.example.musicroom.presentation.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicroom.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    isCreating: Boolean,
    onCreateEvent: (title: String, location: String, description: String?, isPublic: Boolean, startTime: String) -> Unit,
    onDismiss: () -> Unit
) {
    var eventTitle by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var startTime by remember { mutableStateOf("") }
    
    // Validation
    val titleError = when {
        eventTitle.isBlank() -> null
        eventTitle.length < 3 -> "Title must be at least 3 characters"
        eventTitle.length > 100 -> "Title must be less than 100 characters"
        else -> null
    }
    
    // Location dropdown with predefined choices
    var showLocationDropdown by remember { mutableStateOf(false) }
    val predefinedLocations = listOf(
        "E1", "E2", "P1", "P2", "C3", "C4", 
        "Agora", "E3", "C3-Room", "C3-Relax", 
        "C4-rooms", "Elevator-room"
    )

    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        title = {
            Text(
                text = "Create New Event",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Event title
                OutlinedTextField(
                    value = eventTitle,
                    onValueChange = { eventTitle = it },
                    label = { Text("Event Title") },
                    enabled = !isCreating,
                    isError = titleError != null,
                    supportingText = if (titleError != null) {
                        { Text(titleError, color = DarkError) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple,
                        cursorColor = PrimaryPurple
                    )
                )
                
                // Location dropdown
                ExposedDropdownMenuBox(
                    expanded = showLocationDropdown,
                    onExpandedChange = { showLocationDropdown = !showLocationDropdown }
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Location") },
                        enabled = !isCreating,
                        placeholder = { Text("Select location") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLocationDropdown) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = PrimaryPurple
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple,
                            cursorColor = PrimaryPurple
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showLocationDropdown,
                        onDismissRequest = { showLocationDropdown = false }
                    ) {
                        predefinedLocations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc) },
                                onClick = {
                                    location = loc
                                    showLocationDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Start time (you can add a date/time picker later)
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time") },
                    enabled = !isCreating,
                    placeholder = { Text("e.g., 2024-07-15 20:00") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Start Time",
                            tint = PrimaryPurple
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple,
                        cursorColor = PrimaryPurple
                    )
                )
                
                // Description (optional)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    enabled = !isCreating,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        focusedLabelColor = PrimaryPurple,
                        cursorColor = PrimaryPurple
                    )
                )
                
                // Public/Private toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isPublic) "Public Event" else "Private Event",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isPublic) "Everyone can see and join this event" else "Only invited users can join",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        enabled = !isCreating,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryPurple,
                            checkedTrackColor = PrimaryPurple.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (eventTitle.isNotBlank() && location.isNotBlank() && titleError == null) {
                        onCreateEvent(
                            eventTitle,
                            location,
                            description.takeIf { it.isNotBlank() },
                            isPublic,
                            startTime.takeIf { it.isNotBlank() } ?: "TBD"
                        )
                    }
                },
                enabled = !isCreating && eventTitle.isNotBlank() && location.isNotBlank() && titleError == null,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Text("Create Event")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}
