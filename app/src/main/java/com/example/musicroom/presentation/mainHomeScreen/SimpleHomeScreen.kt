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

/**
 * A simplified HomeScreen that is guaranteed not to crash
 * This can be used as a safe fallback if the main HomeScreen has issues
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(user: User) {
    val navController = rememberNavController()

    Scaffold(
        topBar = { SimpleTopBar(user.name) },
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
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = { onTabSelected("home") }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
            label = { Text("Explore") },
            selected = currentRoute == "explore",
            onClick = { onTabSelected("explore") }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
            label = { Text("Create") },
            selected = currentRoute == "create",
            onClick = { onTabSelected("create") }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = { onTabSelected("profile") }
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleTopBar(userName: String) {
    TopAppBar(
        title = { Text("Music Room") },
        actions = {
            IconButton(onClick = { /* Settings action */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }    )
}


