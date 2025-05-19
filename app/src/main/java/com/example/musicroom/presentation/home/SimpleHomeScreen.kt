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

/**
 * A simplified HomeScreen that is guaranteed not to crash
 * This can be used as a safe fallback if the main HomeScreen has issues
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(user: User) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = { SimpleTopBar(user.name) },
        bottomBar = { SimpleBottomNav(selectedTab) { selectedTab = it } }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to Music Room!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hello, ${user.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Display different content based on selected tab
                when (selectedTab) {
                    0 -> {
                        // Home tab content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Text("Home Screen")
                            Text("Join or create music rooms to collaborate with friends")
                        }
                    }
                    1 -> {
                        // Explore tab content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Text("Explore Screen")
                            Text("Discover new music rooms and people")
                        }
                    }
                    2 -> {
                        // Create tab content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Text("Create Room Screen")
                            Text("Create your own music room and invite friends")
                        }
                    }
                    3 -> {
                        // Profile tab content
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Text("Profile Screen")
                            Text("View and edit your profile information")
                        }
                    }
                }
            }
        }
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


@Composable
private fun SimpleBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
            label = { Text("Explore") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
            label = { Text("Create") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
    }
}
