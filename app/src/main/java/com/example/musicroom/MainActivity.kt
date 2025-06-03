package com.example.musicroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import com.example.musicroom.data.models.User
import com.example.musicroom.presentation.home.SimpleHomeScreen
import com.example.musicroom.presentation.theme.MusicRoomTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.musicroom.presentation.home.SimpleHomeScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.musicroom.presentation.auth.LoginScreen
import com.example.musicroom.presentation.auth.SignUpScreen
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    var isAuthenticated by remember { mutableStateOf(false) }
    val navController = rememberNavController()  
    
    // Mock user data for testing purposes
    val mockUser = remember {
        User(
            id = "test-123",
            name = "Test User",
            username = "testuser", // Add username
            photoUrl = "https://example.com/test-avatar.jpg",
            email = "test@gmail.com"
        )
    }

    if (isAuthenticated) {
        // Show home screen when authenticated
        SimpleHomeScreen(user = mockUser)
    } else {
        // Show login screen when not authenticated
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    onLoginClick = { email, password -> /* Handle login */
                        isAuthenticated = true // Simulate successful login
                    },
                    onSignUpClick = {
                        navController.navigate("signup")
                    },
                    onForgotPasswordClick = { /* Handle forgot password */ }
                )
            }
            composable("signup") {
                SignUpScreen(
                    onSignUpClick = { name, email, password -> /* Handle sign up */ },
                    onBackToLoginClick = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}