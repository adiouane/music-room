package com.example.musicroom.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel

sealed class AuthScreenState {
    object Login : AuthScreenState()
    object SignUp : AuthScreenState()
    object ForgotPassword : AuthScreenState()
}

@Composable
fun AuthContainer(onLoginSuccess: () -> Unit) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    var currentScreen by remember { mutableStateOf<AuthScreenState>(AuthScreenState.Login) }    // Handle successful authentication
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoginSuccess -> {
                onLoginSuccess()
                authViewModel.clearState()
            }
            is AuthState.SignUpSuccess -> {
                // For now, also navigate to home after successful signup
                onLoginSuccess()
                authViewModel.clearState()
            }
            is AuthState.GoogleSignInSuccess -> {
                // Navigate to home after successful Google sign-in
                onLoginSuccess()
                authViewModel.clearState()
            }
            else -> { /* No action needed */ }
        }
    }
      when (currentScreen) {
        AuthScreenState.Login -> {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,                onSignUpClick = { 
                    currentScreen = AuthScreenState.SignUp 
                },
                onForgotPasswordClick = { 
                    currentScreen = AuthScreenState.ForgotPassword 
                },
                viewModel = authViewModel
            )
        }        AuthScreenState.SignUp -> {
            SignUpScreen(
                onBackToLoginClick = { 
                    currentScreen = AuthScreenState.Login 
                },
                viewModel = authViewModel
            )
        }
        
        AuthScreenState.ForgotPassword -> {
            ForgotPasswordScreen(
                onBackToLoginClick = { 
                    currentScreen = AuthScreenState.Login 
                },
                viewModel = authViewModel
            )
        }
    }
}
