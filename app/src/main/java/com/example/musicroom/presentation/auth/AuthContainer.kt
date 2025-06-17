package com.example.musicroom.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicroom.data.models.User

@Composable
fun AuthContainer(onLoginSuccess: (User) -> Unit) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    var signupSuccessMessage by remember { mutableStateOf<String?>(null) }// Handle successful authentication (only for actual login, not signup)
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val successState = authState as AuthState.Success
            onLoginSuccess(successState.user)
            authViewModel.clearError()
        }
    }    // Handle signup success - redirect to login with message
    LaunchedEffect(authState) {
        val currentState = authState
        if (currentState is AuthState.SignUpSuccess) {
            // Store the success message and navigate back to login
            signupSuccessMessage = currentState.message
            navController.navigate(AuthScreen.Login.route) {
                popUpTo(AuthScreen.Login.route) { inclusive = true }
            }
            // Clear the auth state after handling
            authViewModel.clearError()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AuthScreen.Login.route
    ) {        composable(route = AuthScreen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password -> 
                    // Clear any signup message when user attempts login
                    signupSuccessMessage = null
                    authViewModel.signIn(email, password)
                },
                onSignUpClick = {
                    navController.navigate(AuthScreen.SignUp.route)
                },
                onForgotPasswordClick = {
                    navController.navigate(AuthScreen.ForgotPassword.route)
                },
                viewModel = authViewModel,
                signupSuccessMessage = signupSuccessMessage
            )
        }

        composable(route = AuthScreen.SignUp.route) {
            SignUpScreen(
                onSignUpClick = { name, email, password -> 
                    authViewModel.signUp(email, password, name)
                },
                onBackToLoginClick = {
                    navController.navigateUp()
                },
                viewModel = authViewModel
            )        }
        
        composable(route = AuthScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackToLoginClick = { 
                    navController.popBackStack() 
                },
                onResetPassword = { email ->
                    // This is now handled by the ViewModel
                },
                viewModel = authViewModel
            )
        }
    }
}
