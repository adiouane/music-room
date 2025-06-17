package com.example.musicroom.presentation.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicroom.presentation.theme.*
import com.example.musicroom.data.auth.DeepLinkManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailConfirmationScreen(
    onConfirmationComplete: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var confirmationState by remember { mutableStateOf<EmailConfirmationState>(EmailConfirmationState.Processing) }
    
    val authState by viewModel.authState.collectAsState()
    
    // Get tokens from DeepLinkManager
    val (accessToken, refreshToken, type) = remember {
        DeepLinkManager.getPasswordResetTokens()
    }
    
    // Process email confirmation when screen loads
    LaunchedEffect(Unit) {
        Log.d("EmailConfirmationScreen", "Screen loaded")
        Log.d("EmailConfirmationScreen", "Access Token: ${accessToken?.take(20)}...")
        Log.d("EmailConfirmationScreen", "Type: $type")
        
        if (accessToken != null && type == "signup") {
            Log.d("EmailConfirmationScreen", "Processing email confirmation...")
            viewModel.confirmEmail(accessToken)
        } else {
            Log.e("EmailConfirmationScreen", "Invalid confirmation token or type")
            confirmationState = EmailConfirmationState.Error("Invalid confirmation link")
        }
    }
      // Handle auth state changes
    LaunchedEffect(authState) {
        val currentState = authState
        when (currentState) {
            is AuthState.Success -> {
                Log.d("EmailConfirmationScreen", "Email confirmation successful")
                confirmationState = EmailConfirmationState.Success
                DeepLinkManager.clearTokens()
                // Wait a moment before navigating to show success message
                kotlinx.coroutines.delay(2000)
                onConfirmationComplete()
            }
            is AuthState.Error -> {
                val errorMessage = currentState.message
                Log.e("EmailConfirmationScreen", "Email confirmation failed: $errorMessage")
                confirmationState = EmailConfirmationState.Error(errorMessage)
            }
            is AuthState.Loading -> {
                confirmationState = EmailConfirmationState.Processing
            }
            else -> {}
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        PrimaryPurple.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center        ) {
            val currentConfirmationState = confirmationState
            when (currentConfirmationState) {
                is EmailConfirmationState.Processing -> {
                    // Processing state
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = PrimaryPurple,
                        strokeWidth = 4.dp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Confirming Your Email",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextPrimary
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Please wait while we verify your email address...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                
                is EmailConfirmationState.Success -> {
                    // Success state
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Email Confirmed!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextPrimary
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Your email has been successfully verified. You can now sign in to your account.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { onConfirmationComplete() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            "Continue to Sign In",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                  is EmailConfirmationState.Error -> {
                    // Error state
                    val errorMessage = currentConfirmationState.message
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Confirmation Failed",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.error
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { onConfirmationComplete() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text(
                            "Back to Login",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

sealed class EmailConfirmationState {
    object Processing : EmailConfirmationState()
    object Success : EmailConfirmationState()
    data class Error(val message: String) : EmailConfirmationState()
}
