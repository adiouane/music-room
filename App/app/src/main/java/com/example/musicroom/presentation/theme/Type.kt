// package com.example.musicroom.ui.theme

// import androidx.compose.material3.Typography
// import androidx.compose.ui.text.TextStyle
// import androidx.compose.ui.text.font.FontFamily
// import androidx.compose.ui.text.font.FontWeight
// import androidx.compose.ui.unit.sp

// // Set of Material typography styles to start with
// val Typography = Typography(
//     bodyLarge = TextStyle(
//         fontFamily = FontFamily.Default,
//         fontWeight = FontWeight.Normal,
//         fontSize = 16.sp,
//         lineHeight = 24.sp,
//         letterSpacing = 0.5.sp
//     )
//     /* Other default text styles to override
//     titleLarge = TextStyle(
//         fontFamily = FontFamily.Default,
//         fontWeight = FontWeight.Normal,
//         fontSize = 22.sp,
//         lineHeight = 28.sp,
//         letterSpacing = 0.sp
//     ),
//     labelSmall = TextStyle(
//         fontFamily = FontFamily.Default,
//         fontWeight = FontWeight.Medium,
//         fontSize = 11.sp,
//         lineHeight = 16.sp,
//         letterSpacing = 0.5.sp
//     )
//     */
// )

package com.example.musicroom.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    )
)