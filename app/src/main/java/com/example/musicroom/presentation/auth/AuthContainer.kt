package com.example.musicroom.presentation.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.musicroom.data.models.User

@Composable
fun AuthContainer(onLoginSuccess: (User) -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthScreen.Login.route
    ) {
        composable(route = AuthScreen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password -> 
                    // Handle login
                },
                onSignUpClick = {
                    navController.navigate(AuthScreen.SignUp.route)
                },
                onForgotPasswordClick = {
                    navController.navigate(AuthScreen.ForgotPassword.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AuthScreen.SignUp.route) {
            SignUpScreen(
                onSignUpClick = { name, email, password -> /* Handle sign up */ },
                onBackToLoginClick = {
                    navController.navigateUp()
                }
            )
        }

        composable(route = AuthScreen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackToLoginClick = { 
                    navController.popBackStack() 
                },
                onResetPassword = { email ->
                    // Handle reset password
                    navController.popBackStack()
                }
            )
        }
    }
}
