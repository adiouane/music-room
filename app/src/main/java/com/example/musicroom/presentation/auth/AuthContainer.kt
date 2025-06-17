package com.example.musicroom.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicroom.data.models.User

@Composable
fun AuthContainer(onLoginSuccess: (User) -> Unit) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()    // Handle successful authentication
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val successState = authState as AuthState.Success
            onLoginSuccess(successState.user)
            authViewModel.clearError()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AuthScreen.Login.route
    ) {
        composable(route = AuthScreen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password -> 
                    authViewModel.signIn(email, password)
                },
                onSignUpClick = {
                    navController.navigate(AuthScreen.SignUp.route)
                },
                onForgotPasswordClick = {
                    navController.navigate(AuthScreen.ForgotPassword.route)
                },
                viewModel = authViewModel
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
