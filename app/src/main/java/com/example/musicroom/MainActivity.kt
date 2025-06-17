package com.example.musicroom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.musicroom.data.models.User
import com.example.musicroom.presentation.mainHomeScreen.SimpleHomeScreen // Updated import
import com.example.musicroom.presentation.theme.MusicRoomTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.musicroom.presentation.auth.AuthContainer
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavController
import com.example.musicroom.presentation.splash.SplashScreen
import com.example.musicroom.presentation.onboarding.OnboardingScreen
import com.example.musicroom.presentation.playlist.PlaylistDetailsScreen
import com.example.musicroom.presentation.auth.ResetPasswordScreen
import com.example.musicroom.presentation.auth.NewPasswordScreen
import com.example.musicroom.data.auth.DeepLinkManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var currentIntent by mutableStateOf<Intent?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntent = intent
        
        setContent {
            MusicRoomTheme {
                val navController = rememberNavController()
                var user by remember { mutableStateOf<User?>(null) }
                var hasSeenOnboarding by remember { mutableStateOf(false) }                // Handle deep links
                LaunchedEffect(currentIntent) {
                    Log.d("MainActivity", "LaunchedEffect triggered with intent: ${currentIntent?.data}")
                    currentIntent?.let { intent ->
                        handleDeepLink(intent, navController)
                    }
                }
                
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
                    
                    // Temporary test route for debugging
                    composable("test_reset") {
                        ResetPasswordScreen(
                            accessToken = "test_token_12345",
                            refreshToken = "test_refresh_12345",
                            onPasswordResetComplete = {
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("home") {
                        user?.let { SimpleHomeScreen(user = it, navController = navController) }
                    }
                      // Add the playlist details route here
                    composable(
                        route = "playlist_details/{playlistId}",
                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                        PlaylistDetailsScreen(
                            playlistId = playlistId,
                            navController = navController
                        )
                    }                    // Password reset route (legacy)
                    composable("reset_password") {
                        Log.d("MainActivity", "ResetPasswordScreen route accessed")
                        
                        ResetPasswordScreen(
                            accessToken = null, // We'll handle the token in the screen itself
                            refreshToken = null,
                            onPasswordResetComplete = {
                                Log.d("MainActivity", "Password reset completed, navigating to auth")
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // New password route (updated for new deep link)
                    composable("new_password") {
                        Log.d("MainActivity", "NewPasswordScreen route accessed")
                        
                        NewPasswordScreen(
                            onPasswordResetComplete = {
                                Log.d("MainActivity", "Password reset completed, navigating to auth")
                                navController.navigate("auth") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        currentIntent = intent
    }    private fun handleDeepLink(intent: Intent, navController: NavController? = null) {
        val data: Uri? = intent.data
        Log.d("MainActivity", "=== DEEP LINK DEBUG ===")
        Log.d("MainActivity", "Intent action: ${intent.action}")
        Log.d("MainActivity", "Intent data: $data")
        Log.d("MainActivity", "NavController: $navController")
        
        if (data != null) {
            Log.d("MainActivity", "Scheme: ${data.scheme}")
            Log.d("MainActivity", "Host: ${data.host}")
            Log.d("MainActivity", "Path: ${data.path}")
            Log.d("MainActivity", "Fragment: ${data.fragment}")
            Log.d("MainActivity", "Query: ${data.query}")
            Log.d("MainActivity", "Full URL: ${data.toString()}")
              if (data.scheme == "musicroom" && (data.host == "reset-password" || data.host == "new-password")) {
                Log.d("MainActivity", "✅ Password reset deep link detected! Host: ${data.host}")
                
                // Extract tokens from URL fragment or query parameters
                val fragment = data.fragment
                
                // Also check query parameters in case tokens are there
                val accessTokenQuery = data.getQueryParameter("access_token")
                val refreshTokenQuery = data.getQueryParameter("refresh_token")
                val typeQuery = data.getQueryParameter("type")
                
                var accessToken: String? = null
                var refreshToken: String? = null
                var type: String? = null
                
                // Try fragment first, then query parameters
                if (fragment != null) {
                    Log.d("MainActivity", "Parsing fragment: $fragment")
                    val params = parseUrlParams(fragment)
                    accessToken = params["access_token"]
                    refreshToken = params["refresh_token"]
                    type = params["type"]
                    Log.d("MainActivity", "Fragment params: $params")
                } else if (accessTokenQuery != null) {
                    Log.d("MainActivity", "Using query parameters")
                    accessToken = accessTokenQuery
                    refreshToken = refreshTokenQuery
                    type = typeQuery
                }
                
                Log.d("MainActivity", "Final tokens - Access: ${accessToken?.take(20)}..., Refresh: ${refreshToken?.take(20)}..., Type: $type")
                
                if (accessToken != null && type == "recovery") {
                    Log.d("MainActivity", "🚀 Navigating to password reset screen")
                    
                    // Store tokens in DeepLinkManager
                    DeepLinkManager.setPasswordResetTokens(accessToken, refreshToken, type)
                    
                    try {
                        // Navigate to appropriate screen based on host
                        val route = if (data.host == "new-password") "new_password" else "reset_password"
                        Log.d("MainActivity", "Using route: $route for host: ${data.host}")
                        
                        navController?.navigate(route) {
                            // Clear back stack to prevent going back to link handling
                            popUpTo(0) { inclusive = true }
                        }
                        Log.d("MainActivity", "✅ Navigation successful")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "❌ Navigation failed: ${e.message}", e)
                    }
                } else {
                    Log.w("MainActivity", "❌ Missing required tokens or wrong type. Access token: ${accessToken != null}, Type: $type")
                }
            } else {
                Log.d("MainActivity", "❌ Not a password reset deep link")
            }
        } else {
            Log.d("MainActivity", "❌ No intent data found")
        }
        Log.d("MainActivity", "=== END DEEP LINK DEBUG ===")
    }
      private fun parseUrlParams(fragment: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        Log.d("MainActivity", "Parsing fragment: $fragment")
        
        fragment.split("&").forEach { param ->
            val keyValue = param.split("=", limit = 2)
            if (keyValue.size == 2) {
                val key = keyValue[0]
                val value = try {
                    java.net.URLDecoder.decode(keyValue[1], "UTF-8")
                } catch (e: Exception) {
                    keyValue[1] // fallback to original if decoding fails
                }
                params[key] = value
                Log.d("MainActivity", "Param: $key = ${value.take(20)}...")
            }
        }
        return params
    }
}