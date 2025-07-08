package com.example.musicroomi.presentation.auth

sealed class AuthScreen(val route: String) {
    object Login : AuthScreen("login")
    object SignUp : AuthScreen("signup")
    object ForgotPassword : AuthScreen("forgot_password")
}