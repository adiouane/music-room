package com.example.musicroom.presentation.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicroom.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    isCreating: Boolean,
    onCreateEvent: (title: String, location: String, description: String?, isPublic: Boolean, startTime: String, endTime: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var eventTitle by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    
    // Date and Time states
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    var startDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var startTimeHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var startTimeMinute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    
    var endDateMillis by remember { mutableLongStateOf(System.currentTimeMillis() + (2 * 60 * 60 * 1000)) } // +2 hours
    var endTimeHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 2) }
    var endTimeMinute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    
    var hasEndTime by remember { mutableStateOf(false) }
    
    // Format date and time display
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    val startDateDisplay = dateFormat.format(Date(startDateMillis))
    val startTimeDisplay = String.format("%02d:%02d", startTimeHour, startTimeMinute)
    
    val endDateDisplay = dateFormat.format(Date(endDateMillis))
    val endTimeDisplay = String.format("%02d:%02d", endTimeHour, endTimeMinute)
    
    // Validation
    val titleError = when {
        eventTitle.isBlank() -> "Title is required"
        eventTitle.length < 3 -> "Title must be at least 3 characters"
        eventTitle.length > 100 -> "Title must be less than 100 characters"
        else -> null
    }
    
    val locationError = when {
        location.isBlank() -> "Location is required"
        else -> null
    }
    
    // Location dropdown with predefined choices matching backend
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
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Event title
                OutlinedTextField(
                    value = eventTitle,
                    onValueChange = { eventTitle = it },
                    label = { Text("Event Title *") },
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
                        label = { Text("Location *") },
                        enabled = !isCreating,
                        placeholder = { Text("Select location") },
                        isError = locationError != null,
                        supportingText = if (locationError != null) {
                            { Text(locationError, color = DarkError) }
                        } else null,
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
                
                // Start Date and Time Section
                Text(
                    text = "Event Start Time *",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Start Date
                    OutlinedTextField(
                        value = startDateDisplay,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Date") },
                        enabled = !isCreating,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Start Date",
                                tint = PrimaryPurple
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!isCreating) {
                                    Modifier.clickable { showStartDatePicker = true }
                                } else Modifier
                            ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple,
                            cursorColor = PrimaryPurple
                        )
                    )
                    
                    // Start Time
                    OutlinedTextField(
                        value = startTimeDisplay,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Time") },
                        enabled = !isCreating,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Start Time",
                                tint = PrimaryPurple
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!isCreating) {
                                    Modifier.clickable { showStartTimePicker = true }
                                } else Modifier
                            ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            focusedLabelColor = PrimaryPurple,
                            cursorColor = PrimaryPurple
                        )
                    )
                }
                
                // Voting concept info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurple.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = "Voting Info",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Track voting will be displayed between event start and end time",
                            color = PrimaryPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // End Time Toggle and Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Set End Time (optional)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = hasEndTime,
                        onCheckedChange = { hasEndTime = it },
                        enabled = !isCreating,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryPurple,
                            checkedTrackColor = PrimaryPurple.copy(alpha = 0.5f)
                        )
                    )
                }
                
                if (hasEndTime) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // End Date
                        OutlinedTextField(
                            value = endDateDisplay,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("End Date") },
                            enabled = !isCreating,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "End Date",
                                    tint = PrimaryPurple
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (!isCreating) {
                                        Modifier.clickable { showEndDatePicker = true }
                                    } else Modifier
                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple,
                                cursorColor = PrimaryPurple
                            )
                        )
                        
                        // End Time
                        OutlinedTextField(
                            value = endTimeDisplay,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("End Time") },
                            enabled = !isCreating,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "End Time",
                                    tint = PrimaryPurple
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (!isCreating) {
                                        Modifier.clickable { showEndTimePicker = true }
                                    } else Modifier
                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryPurple,
                                focusedLabelColor = PrimaryPurple,
                                cursorColor = PrimaryPurple
                            )
                        )
                    }
                }
                
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
                    if (titleError == null && locationError == null) {
                        // Format start time
                        val startCalendar = Calendar.getInstance().apply {
                            timeInMillis = startDateMillis
                            set(Calendar.HOUR_OF_DAY, startTimeHour)
                            set(Calendar.MINUTE, startTimeMinute)
                            set(Calendar.SECOND, 0)
                        }
                        val startTimeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(startCalendar.time)
                        
                        // Format end time if enabled
                        val endTimeFormatted = if (hasEndTime) {
                            val endCalendar = Calendar.getInstance().apply {
                                timeInMillis = endDateMillis
                                set(Calendar.HOUR_OF_DAY, endTimeHour)
                                set(Calendar.MINUTE, endTimeMinute)
                                set(Calendar.SECOND, 0)
                            }
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(endCalendar.time)
                        } else null
                        
                        onCreateEvent(
                            eventTitle.trim(),
                            location,
                            description.takeIf { it.isNotBlank() }?.trim(),
                            isPublic,
                            startTimeFormatted,
                            endTimeFormatted
                        )
                    }
                },
                enabled = !isCreating && titleError == null && locationError == null,
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

    // Simple Date and Time Picker Dialogs (without experimental features)
    if (showStartDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                startDateMillis = calendar.timeInMillis
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false },
            initialYear = Calendar.getInstance().apply { timeInMillis = startDateMillis }.get(Calendar.YEAR),
            initialMonth = Calendar.getInstance().apply { timeInMillis = startDateMillis }.get(Calendar.MONTH),
            initialDay = Calendar.getInstance().apply { timeInMillis = startDateMillis }.get(Calendar.DAY_OF_MONTH)
        )
    }

    if (showEndDatePicker) {
        SimpleDatePickerDialog(
            onDateSelected = { year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                endDateMillis = calendar.timeInMillis
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
            initialYear = Calendar.getInstance().apply { timeInMillis = endDateMillis }.get(Calendar.YEAR),
            initialMonth = Calendar.getInstance().apply { timeInMillis = endDateMillis }.get(Calendar.MONTH),
            initialDay = Calendar.getInstance().apply { timeInMillis = endDateMillis }.get(Calendar.DAY_OF_MONTH)
        )
    }

    if (showStartTimePicker) {
        SimpleTimePickerDialog(
            onTimeSelected = { hour, minute ->
                startTimeHour = hour
                startTimeMinute = minute
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false },
            initialHour = startTimeHour,
            initialMinute = startTimeMinute
        )
    }

    if (showEndTimePicker) {
        SimpleTimePickerDialog(
            onTimeSelected = { hour, minute ->
                endTimeHour = hour
                endTimeMinute = minute
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false },
            initialHour = endTimeHour,
            initialMinute = endTimeMinute
        )
    }
}

@Composable
private fun SimpleDatePickerDialog(
    onDateSelected: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit,
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int
) {
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    var selectedMonth by remember { mutableIntStateOf(initialMonth) }
    var selectedDay by remember { mutableIntStateOf(initialDay) }
    
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear..currentYear + 5).toList()
    val days = (1..31).toList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Date",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Year selection
                Text("Year", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                LazyRow {
                    items(years) { year ->
                        FilterChip(
                            onClick = { selectedYear = year },
                            label = { Text(year.toString()) },
                            selected = selectedYear == year,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                // Month selection
                Text("Month", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                LazyRow {
                    items(months.size) { index ->
                        FilterChip(
                            onClick = { selectedMonth = index },
                            label = { Text(months[index]) },
                            selected = selectedMonth == index,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                // Day selection
                Text("Day", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                LazyRow {
                    items(days) { day ->
                        FilterChip(
                            onClick = { selectedDay = day },
                            label = { Text(day.toString()) },
                            selected = selectedDay == day,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDateSelected(selectedYear, selectedMonth, selectedDay) }
            ) {
                Text("OK", color = PrimaryPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
private fun SimpleTimePickerDialog(
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }
    
    val hours = (0..23).toList()
    val minutes = (0..59 step 5).toList() // 5-minute intervals
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Time",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hour selection
                Text("Hour", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                LazyRow {
                    items(hours) { hour ->
                        FilterChip(
                            onClick = { selectedHour = hour },
                            label = { Text(String.format("%02d", hour)) },
                            selected = selectedHour == hour,
                            modifier = Modifier.padding(horizontal = 2.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                // Minute selection
                Text("Minute", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                LazyRow {
                    items(minutes) { minute ->
                        FilterChip(
                            onClick = { selectedMinute = minute },
                            label = { Text(String.format("%02d", minute)) },
                            selected = selectedMinute == minute,
                            modifier = Modifier.padding(horizontal = 2.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onTimeSelected(selectedHour, selectedMinute) }
            ) {
                Text("OK", color = PrimaryPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}
