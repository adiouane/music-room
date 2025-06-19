package com.example.musicroom.presentation.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicroom.presentation.theme.*
import com.example.musicroom.data.auth.DeepLinkManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPasswordScreen(
    onPasswordResetComplete: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsState()
    
    // Get tokens from DeepLinkManager
    val (accessToken, refreshToken, type) = remember {
        DeepLinkManager.getPasswordResetTokens()
    }
    
    // Log the received tokens for debugging
    LaunchedEffect(Unit) {
        Log.d("NewPasswordScreen", "Screen loaded")
        Log.d("NewPasswordScreen", "Access Token: ${accessToken?.take(20)}...")
        Log.d("NewPasswordScreen", "Access Token length: ${accessToken?.length ?: 0}")
        Log.d("NewPasswordScreen", "Refresh Token: ${refreshToken?.take(20)}...")
        Log.d("NewPasswordScreen", "Deep link type: $type")
        
        if (accessToken?.startsWith("eyJ") == false) {
            Log.e("NewPasswordScreen", "WARNING: Access token doesn't look like a JWT!")
        }
    }
      // Handle password reset completion
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            Log.d("NewPasswordScreen", "Password update successful, clearing tokens")
            DeepLinkManager.clearTokens()
            onPasswordResetComplete()
        }
    }
    
    // Validation functions
    fun validatePassword(): Boolean {
        passwordError = when {
            newPassword.length < 6 -> "Password must be at least 6 characters"
            else -> ""
        }
        return passwordError.isEmpty()
    }
    
    fun validateConfirmPassword(): Boolean {
        confirmPasswordError = when {
            confirmPassword != newPassword -> "Passwords do not match"
            else -> ""
        }
        return confirmPasswordError.isEmpty()
    }
      fun resetPassword() {
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()
        if (isPasswordValid && isConfirmPasswordValid && !accessToken.isNullOrEmpty()) {
            Log.d("NewPasswordScreen", "Updating password with token: ${accessToken.take(20)}...")
            viewModel.updatePassword(accessToken, newPassword)
        } else if (accessToken.isNullOrEmpty()) {
            Log.e("NewPasswordScreen", "No access token available")
        }    }
    
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "Create New Password",                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = TextPrimary
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Please enter your new password below",                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // New Password Field
            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    if (passwordError.isNotEmpty()) validatePassword()
                },                label = { Text("New Password", color = TextPrimary) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Password",
                        tint = PrimaryPurple
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility 
                                         else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" 
                                               else "Show password",
                            tint = PrimaryPurple
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None 
                                     else PasswordVisualTransformation(),
                isError = passwordError.isNotEmpty(),
                supportingText = if (passwordError.isNotEmpty()) {
                    { Text(passwordError, color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = PrimaryPurple
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    if (confirmPasswordError.isNotEmpty()) validateConfirmPassword()
                },
                label = { Text("Confirm New Password", color = TextPrimary) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Confirm Password",
                        tint = PrimaryPurple
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility 
                                         else Icons.Filled.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" 
                                               else "Show password",
                            tint = PrimaryPurple
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None 
                                     else PasswordVisualTransformation(),
                isError = confirmPasswordError.isNotEmpty(),
                supportingText = if (confirmPasswordError.isNotEmpty()) {
                    { Text(confirmPasswordError, color = MaterialTheme.colorScheme.error) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = PrimaryPurple
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error message for general errors
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Update Password Button
            Button(
                onClick = { resetPassword() },
                enabled = authState !is AuthState.Loading && 
                         newPassword.isNotEmpty() && 
                         confirmPassword.isNotEmpty() &&
                         !accessToken.isNullOrEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    contentColor = TextPrimary
                )
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TextPrimary
                    )
                } else {
                    Text(
                        "Update Password",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            
            // Debug info (can be removed in production)
            if (accessToken.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "⚠️ No reset token found. Please use the reset link from your email.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
