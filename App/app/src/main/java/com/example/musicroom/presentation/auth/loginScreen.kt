package com.example.musicroomi.presentation.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.musicroomi.R
import com.example.musicroomi.components.GoogleButton
import com.example.musicroomi.presentation.theme.*
import com.example.musicroom.data.auth.GoogleAuthUiClient
import com.example.musicroom.data.auth.GoogleSignInResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
/**
 * ========================================================================================
 * LOGIN SCREEN COMPONENT
 * ========================================================================================
 * 
 * This is the main login screen with email/password and Google authentication.
 * Features beautiful gradient background and comprehensive form validation.
 * 
 * 🎯 CURRENT FUNCTIONALITY:
 * ========================================================================================
 * ✅ Email/Password login with mock backend (ready for real API)
 * ✅ Google Sign-In integration (functional)
 * ✅ Form validation and user feedback
 * ✅ Loading states and error handling
 * ✅ Navigation to signup and forgot password screens
 * 
 * 🔄 FOR DEVELOPERS:
 * ========================================================================================
 * - Authentication logic is in AuthViewModel
 * - Backend integration ready in AuthApiService (just update URL)
 * - Google Sign-In configured via GoogleAuthUiClient
 * - Navigation handled by parent AuthContainer
 * 
 * 📱 UI COMPONENTS:
 * ========================================================================================
 * - Gradient background with app branding
 * - Email input with validation
 * - Password input with show/hide toggle
 * - Primary login button with loading state
 * - Google Sign-In button (custom component)
 * - Navigation links for signup/forgot password
 * 
 * 🧪 TESTING WITH MOCK DATA:
 * ========================================================================================
 * Email: test@example.com / Password: password123 → Success
 * Email: fail@example.com / Password: any → Login failure
 * Any other email/password → Success with mock user data
 * ========================================================================================
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,        // Callback when login succeeds → Navigate to home
    onSignUpClick: () -> Unit,         // Callback to navigate to sign up screen
    onForgotPasswordClick: () -> Unit, // Callback to navigate to forgot password screen
    viewModel: AuthViewModel = hiltViewModel() // Add viewModel parameter with default
) {
    // ============================================================================
    // GOOGLE SIGN-IN SETUP
    // ============================================================================
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Create GoogleAuthUiClient locally
    val googleAuthUiClient = remember { GoogleAuthUiClient(context) }
    
    // Activity result launcher for Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    val signInResult = googleAuthUiClient.signInWithIntent(result.data)
                    // Handle the result
                    if (signInResult.data != null) {
                        // Success - Get real Firebase ID token
                        try {
                            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
                            if (idToken != null) {
                                viewModel.signInWithGoogle(idToken, null)
                            } else {
                                Log.e("LoginScreen", "❌ Failed to get ID token")
                            }
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "❌ Error getting ID token: ${e.message}")
                        }
                    } else {
                        // Error - show error message
                        Log.e("LoginScreen", "Google Sign-In failed: ${signInResult.errorMessage}")
                    }
                }
            }
        }
    )
    
    // ============================================================================
    // STATE MANAGEMENT
    // ============================================================================
    var email by remember { mutableStateOf("") }           // Email input state
    var password by remember { mutableStateOf("") }        // Password input state
    var passwordVisible by remember { mutableStateOf(false) } // Password visibility toggle
    
    // Observe authentication state from ViewModel
    val authState by viewModel.authState.collectAsState()
    
    // ============================================================================
    // SIDE EFFECTS - Handle authentication results
    // ============================================================================
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoginSuccess -> {
                Log.d("LoginScreen", "✅ Login successful, navigating to home")
                onLoginSuccess() // Navigate to home screen
                viewModel.clearState() // Reset auth state
            }
            is AuthState.GoogleSignInSuccess -> {
                Log.d("LoginScreen", "✅ Google Sign-In successful, navigating to home")
                onLoginSuccess() // Navigate to home screen
                viewModel.clearState() // Reset auth state
            }
            else -> {
                // Other states handled in UI (loading, error display)
            }
        }
    }    
    // ============================================================================
    // UI LAYOUT - Full screen with gradient background
    // ============================================================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(onboardingGradient) // Custom gradient from theme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Standard padding for mobile screens
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ========================================================================
            // SCROLLABLE CONTENT - Prevents keyboard overlap issues
            // ========================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()), // Enable scrolling
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // Consistent spacing
            ) {
                // ================================================================
                // HEADER SECTION - App logo and welcome message
                // ================================================================
                HeaderSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 36.dp) // Extra top padding for status bar
                )
                
                // ================================================================
                // LOGIN FORM SECTION - Email/Password inputs
                // ================================================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent // Transparent to show gradient
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // ========================================================
                        // EMAIL INPUT FIELD
                        // ========================================================
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it }, // Update state on text change
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, "Email") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email, // Email keyboard layout
                                imeAction = ImeAction.Next // Next button moves to password field
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                // Custom colors to match app theme
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        
                        // ========================================================
                        // PASSWORD INPUT FIELD - With show/hide toggle
                        // ========================================================
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it }, // Update state on text change
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                            trailingIcon = {
                                // Toggle button to show/hide password
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        Icons.Default.RemoveRedEye,
                                        if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            // Show/hide password text based on toggle state
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password, // Password keyboard
                                imeAction = ImeAction.Done // Done button triggers login
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                // Custom colors to match app theme
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )                        
                        // ========================================================
                        // ACTION BUTTONS SECTION - Login, Forgot Password, Google Sign-In
                        // ========================================================
                        ActionButtons(
                            onForgotPasswordClick = onForgotPasswordClick,
                            onLoginClick = { 
                                Log.d("LoginScreen", "🔐 Login button clicked - Starting authentication")
                                // Trigger login with trimmed email and password
                                // ViewModel handles API call and state management
                                viewModel.login(email.trim(), password)
                            },                            onGoogleSignInClick = { 
                                Log.d("LoginScreen", "🔗 Google Sign-In button clicked - Starting Google authentication")
                                try {
                                    val signInIntent = googleAuthUiClient.getSignInIntent()
                                    googleSignInLauncher.launch(signInIntent)
                                } catch (e: Exception) {
                                    Log.e("LoginScreen", "❌ Error launching Google Sign-In: ${e.message}")
                                }
                            },
                            isLoading = authState is AuthState.Loading, // Show loading state
                            email = email,
                            password = password
                        )
                    }
                }
                
                // ================================================================
                // ERROR DISPLAY SECTION - Show authentication errors to user
                // ================================================================
                if (authState is AuthState.Error) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer // Error theme color
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Login Error",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            // Simplified debug information for API-based auth
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Debug Information:",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "• Error: ${(authState as AuthState.Error).message}",
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Email: $email",
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Auth Type: Simple API",
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                  // Sign Up Section - now functional
                SignUpSection(onSignUpClick = onSignUpClick)
            }
        }
    }
}

/**
 * ============================================================================
 * HEADER SECTION COMPONENT
 * ============================================================================
 * 
 * Displays the app branding with logo and welcome message.
 * Features centered layout with consistent spacing and theme colors.
 * 
 * 🎨 DESIGN ELEMENTS:
 * - Music note icon as app logo
 * - App name "Music Room" with large typography
 * - Subtitle "Connect through music" with reduced opacity
 * - Proper padding and alignment for professional look
 * ============================================================================
 */
@Composable
private fun HeaderSection(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Fixed height for consistent layout
        ) {       
            // ================================================================
            // APP BRANDING OVERLAY - Logo, title, and subtitle
            // ================================================================
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App logo icon
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "App Logo",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary // Use theme primary color
                )
                
                // App name
                Text(
                    text = "Music Room",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // App subtitle/tagline
                Text(
                    text = "Connect through music",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), // Subtle opacity
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * ============================================================================
 * ACTION BUTTONS COMPONENT
 * ============================================================================
 * 
 * Contains all interactive buttons for the login screen:
 * - Primary login button with validation and loading state
 * - Forgot password link
 * - Google Sign-In button (custom component)
 * - Sign up navigation link
 * 
 * 🔧 FUNCTIONALITY:
 * - Form validation before enabling login button
 * - Loading state management during authentication
 * - Proper spacing and visual hierarchy
 * - Accessibility support with content descriptions
 * 
 * 📱 RESPONSIVE DESIGN:
 * - Full width buttons for mobile optimization
 * - Proper touch targets (minimum 48dp height)
 * - Consistent spacing between elements
 * ============================================================================
 */
@Composable
private fun ActionButtons(
    onForgotPasswordClick: () -> Unit,  // Callback for forgot password navigation
    onLoginClick: () -> Unit,           // Callback for email/password login
    onGoogleSignInClick: () -> Unit,    // Callback for Google authentication
    isLoading: Boolean = false,         // Loading state from authentication
    email: String,                      // Current email input value
    password: String                    // Current password input value
) {
    // ========================================================================
    // FORGOT PASSWORD LINK - Right-aligned for UX convention
    // ========================================================================
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End // Align to right side
    ) {
        TextButton(
            onClick = onForgotPasswordClick,
            enabled = !isLoading // Disable during authentication
        ) {
            Text("Forgot Password?")
        }
    }
    
    // ========================================================================
    // PRIMARY LOGIN BUTTON - Main call-to-action
    // ========================================================================
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp), // Standard button height for good touch target
        // Enable only when form is valid and not loading
        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary // Use theme color
        )
    ) {
        if (isLoading) {
            // Show loading spinner during authentication
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("Login")
        }
    }
    
    // ========================================================================
    // SOCIAL LOGIN SECTION - Google Sign-In integration
    // ========================================================================
    SocialLoginSection(
        onGoogleSignInClick = onGoogleSignInClick
    )
    
    Spacer(modifier = Modifier.height(16.dp)) // Bottom spacing
}

/**
 * ============================================================================
 * SOCIAL LOGIN SECTION COMPONENT
 * ============================================================================
 * 
 * Provides alternative authentication methods (currently Google Sign-In).
 * Features visual separator and branded Google button.
 * 
 * 🔗 GOOGLE SIGN-IN INTEGRATION:
 * - Uses GoogleAuthUiClient for OAuth flow
 * - Custom GoogleButton component with proper branding
 * - Handles authentication result in ViewModel
 * 
 * 💡 FOR DEVELOPERS:
 * - Google Sign-In is fully functional and configured
 * - Add other social providers here if needed (Facebook, Apple, etc.)
 * - Maintain consistent visual hierarchy with login button
 * ============================================================================
 */
@Composable
private fun SocialLoginSection(
    onGoogleSignInClick: () -> Unit      // Callback for Google authentication
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // ====================================================================
        // VISUAL SEPARATOR - "OR" divider between login methods
        // ====================================================================
        Text(
            text = "OR",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), // Subtle color
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // Use only the parameters that GoogleButton accepts
        GoogleButton(
            onClick = onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SignUpSection(onSignUpClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Don't have an account? ",
            style = MaterialTheme.typography.bodyMedium
        )
        TextButton(onClick = onSignUpClick) {
            Text("Sign Up")
        }
    }
}
