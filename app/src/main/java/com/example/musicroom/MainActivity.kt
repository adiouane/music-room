package com.example.musicroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicroom.data.models.User
import com.example.musicroom.presentation.theme.MusicRoomTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.musicroom.presentation.auth.AuthViewModel
import com.example.musicroom.presentation.auth.AuthContainer

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
                    val viewModel: AuthViewModel = hiltViewModel()
                    val authState by viewModel.authState.collectAsState()
                    
                    if (authState.user != null) {
                        // User is logged in, show main app content
                        MainContent(user = authState.user!!)
                    } else {
                        // User is not logged in, show auth container
                        AuthContainer(
                            onLoginSuccess = {
                                // This will be handled by the viewModel updating the authState
                            }
                        )
                    }
                }
            }
        }
    }
}

// Add the MainContent composable function
@Composable
fun MainContent(user: User) {
    Scaffold { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // This is a placeholder for the main app content
            // You'll replace this with your actual app navigation and content
            Text(
                text = "Welcome, ${user.name}!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}