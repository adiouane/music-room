package com.example.musicroom.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musicroom.data.models.User
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.musicroom.presentation.explore.ExploreScreen
import com.example.musicroom.presentation.profile.ProfileScreen
import com.example.musicroom.presentation.room.CreateRoomScreen
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.musicroom.presentation.theme.*

/**
 * A simplified HomeScreen that is guaranteed not to crash
 * This can be used as a safe fallback if the main HomeScreen has issues
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(user: User) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { 
            SimpleBottomNav(
                currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route,
                onTabSelected = { route -> navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }}
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeTabScreen() }
            composable("explore") { ExploreScreen() }
            composable("create") { CreateRoomScreen() }
            composable("profile") { ProfileScreen(user) }
        }
    }
}

@Composable
private fun SimpleBottomNav(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = DarkBackground,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("home", Icons.Default.Home, "Home"),
            Triple("explore", Icons.Default.Search, "Explore"),
            Triple("create", Icons.Default.Add, "Create"),
            Triple("profile", Icons.Default.Person, "Profile")
        )

        items.forEach { (route, icon, label) ->
            val selected = currentRoute == route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) TextPrimary else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (selected) TextPrimary else TextSecondary,
                        fontSize = 12.sp
                    )
                },
                selected = selected,
                onClick = { onTabSelected(route) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PrimaryPurple
                )
            )
        }
    }
}
