package com.example.musicroom.presentation.mainHomeScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.musicroom.data.models.User
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavController
import androidx.navigation.NavHostController  // Add this import
import com.example.musicroom.presentation.explore.ExploreScreen
import com.example.musicroom.presentation.profile.ProfileScreen
import com.example.musicroom.presentation.room.CreateRoomScreen
import com.example.musicroom.presentation.theme.* 
import com.example.musicroom.presentation.room.*
import com.example.musicroom.presentation.playlist.PlaylistDetailsScreen
import com.example.musicroom.presentation.home.HomeTabScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(user: User, navController: NavController) {
    val innerNavController = rememberNavController()  // Create a separate nav controller for inner navigation
    
    Scaffold(
        bottomBar = { 
            SimpleBottomNav(
                currentRoute = innerNavController.currentBackStackEntryAsState().value?.destination?.route,
                onTabSelected = { route -> 
                    innerNavController.navigate(route) {
                        popUpTo(innerNavController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = innerNavController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { 
                HomeTabScreen(navController = navController)  // Pass the main navController for playlist navigation
            }
            composable("explore") { 
                ExploreScreen() 
            }
            composable("create") { 
                CreateRoomScreen() 
            }
            composable("profile") { 
                ProfileScreen(user) 
            }
            
            composable(
                route = "room/{roomId}",
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId")
                RoomDetailScreen(roomId = roomId, onBackClick = { innerNavController.navigateUp() })
            }
            
            composable(
                route = "playlist_details/{playlistId}",
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                PlaylistDetailsScreen(
                    playlistId = playlistId,
                    navController = navController  // Use main navController
                )
            }
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
