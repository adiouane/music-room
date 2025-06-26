package com.example.musicroom.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Brand Colors
val PrimaryPurple = Color(0xFF9C27B0)
val DeepPurple = Color(0xFF673AB7)
val DarkPurple = Color(0xFF4A148C)
val AccentPurple = Color(0xFFAB47BC)

// UI Colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1D1D1D)
val DarkError = Color(0xFFCF6679)

// Text Colors
val TextPrimary = Color.White
val TextSecondary = Color.White.copy(alpha = 0.7f)

// Gradient Colors
val purpleGradient = Brush.linearGradient(
    listOf(
        PrimaryPurple,
        DeepPurple
    )
)

// Make gradient public
public val onboardingGradient = Brush.verticalGradient(
    listOf(
        DarkBackground,
        PrimaryPurple.copy(alpha = 0.2f),
        DeepPurple.copy(alpha = 0.1f),
        DarkBackground
    )
)

// Add to existing gradients
val signUpGradient = Brush.verticalGradient(
    listOf(
        PrimaryPurple,
        DeepPurple
    )
)

// Dark Theme Colors
val DarkPrimary = PrimaryPurple
val DarkSecondary = DeepPurple
val DarkOnPrimary = Color.White
val DarkOnSecondary = Color.White
val DarkOnBackground = Color.White
val DarkOnSurface = Color.White

// Light Theme Colors
val LightPrimary = PrimaryPurple
val LightSecondary = DeepPurple
val LightBackground = Color.White
val LightSurface = Color.White
val LightError = Color(0xFFB00020)
val LightOnPrimary = Color.White
val LightOnSecondary = Color.Black
val LightOnBackground = Color.Black
val LightOnSurface = Color.Black