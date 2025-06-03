package com.example.musicroom.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicroom.data.models.User

/**
 * This component has been disabled as authentication is being implemented later.
 * This is just a placeholder to maintain the package structure.
 */
@Composable
fun AuthContainer(
    onLoginSuccess: (User) -> Unit,
) {
    // Immediately call onLoginSuccess with a mock user
    // This is a simplified approach as authentication is disabled
    val mockUser = User(
        id = "test-123",
        name = "Test User",
        username = "testuser", // Add username
        photoUrl = "https://example.com/test-avatar.jpg",
        email = "test@gmail.com"
    )
    
    LaunchedEffect(Unit) {
        onLoginSuccess(mockUser)
    }
    
    // No UI is rendered, immediately move to the home screen
}
