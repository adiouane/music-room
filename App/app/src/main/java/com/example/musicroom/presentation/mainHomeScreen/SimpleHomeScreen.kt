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
import com.example.musicroom.presentation.home.HomeScreen  // Updated import to use new HomeScreen
import com.example.musicroom.presentation.player.NowPlayingScreen
import com.example.musicroom.presentation.music.MusicSearchScreen

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
                HomeScreen(navController = navController)  // Pass the main navController for navigation
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
                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                RoomDetailScreen(
                    roomId = roomId,
                    navController = innerNavController  // Use correct parameter name
                )
            }
            
            composable(
                route = "playlist/{playlistId}",
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                PlaylistDetailsScreen(
                    playlistId = playlistId,
                    navController = innerNavController
                )
            }
        }
    }
}

@Composable
fun SimpleBottomNav(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = { onTabSelected("home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = PrimaryPurple.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
            label = { Text("Explore") },
            selected = currentRoute == "explore",
            onClick = { onTabSelected("explore") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = PrimaryPurple.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
            label = { Text("Create") },
            selected = currentRoute == "create",
            onClick = { onTabSelected("create") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = PrimaryPurple.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = { onTabSelected("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryPurple,
                selectedTextColor = PrimaryPurple,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary,
                indicatorColor = PrimaryPurple.copy(alpha = 0.2f)
            )
        )
    }
}
