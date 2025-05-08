package com.example.musicroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.musicroom.data.models.User
import com.example.musicroom.presentation.auth.AuthContainer
import com.example.musicroom.presentation.auth.AuthViewModel
import com.example.musicroom.presentation.main.MainContent
import com.example.musicroom.presentation.theme.MusicRoomTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicRoomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent()
                }
            }
        }
    }
}

@Composable
fun AppContent() {
    val viewModel: AuthViewModel = hiltViewModel()
    val authState by viewModel.authState.collectAsState()
    
    if (authState.user != null) {
        // User is logged in, show main app content
        MainContent(user = authState.user!!)
    } else {
        // User is not logged in, show auth container
        AuthContainer(
            onLoginSuccess = { user ->
                // Handle login success, e.g., navigate to main content
                // This is handled in the AuthContainer via the viewModel
            }
        )
        // No need to pass onLoginSuccess as the viewModel will handle state updates
    }
}