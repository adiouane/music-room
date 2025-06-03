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
import com.example.musicroom.presentation.splash.SplashScreen
import com.example.musicroom.presentation.onboarding.OnboardingScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicRoomTheme {
                val navController = rememberNavController()
                var user by remember { mutableStateOf<User?>(null) }
                var hasSeenOnboarding by remember { mutableStateOf(false) }
                
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(
                            onNavigateToOnboarding = { navController.navigate("onboarding") }
                        )
                    }
                    
                    composable("onboarding") {
                        OnboardingScreen(
                            onFinish = {
                                hasSeenOnboarding = true
                                navController.navigate("auth") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("auth") {
                        AuthContainer(
                            onLoginSuccess = { newUser ->
                                user = newUser
                                navController.navigate("home") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("home") {
                        user?.let { SimpleHomeScreen(user = it) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthContainer(onLoginSuccess: (User) -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginClick = { email, password -> /* Handle login */
                    val mockUser = User(
                        id = "test-123",
                        name = "Test User",
                        username = "testuser",
                        photoUrl = "https://example.com/test-avatar.jpg",
                        email = email
                    )
                    onLoginSuccess(mockUser) // Simulate successful login
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