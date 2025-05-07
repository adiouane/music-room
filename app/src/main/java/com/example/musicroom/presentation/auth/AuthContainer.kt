package com.example.musicroom.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Container component for authentication flow.
 * Manages navigation between login and signup screens.
 */
@Composable
fun AuthContainer(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var isLoginMode by remember { mutableStateOf(true) }
    
    val authState by viewModel.authState.collectAsState()
    
    // Handle login success
    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            onLoginSuccess()
        }
    }
    
    if (isLoginMode) {
        LoginScreen(
            onLoginClick = { email, password ->
                viewModel.signInWithEmail(email, password)
            },
            onSignUpClick = { 
                // Switch to sign up mode
                isLoginMode = false
            },
            onForgotPasswordClick = {
                // TODO: Implement forgot password flow
                viewModel.sendPasswordResetEmail("") // This needs a proper implementation
            }
        )
    } else {
        SignUpScreen(
            onSignUpClick = { name, email, password ->
                viewModel.signUpWithEmail(name, email, password)
            },
            onBackToLoginClick = {
                // Switch back to login mode
                isLoginMode = true
            }
        )
    }
}
