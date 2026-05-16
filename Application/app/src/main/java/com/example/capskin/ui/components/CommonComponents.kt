package com.example.capskin.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.getValue
import com.example.capskin.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("홈") },
            selected = currentDestination == Screen.Home.route,
            onClick = { 
                if (currentDestination != Screen.Home.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Camera, contentDescription = null) },
            label = { Text("분석") },
            selected = currentDestination == Screen.Camera.route,
            onClick = {
                if (currentDestination != Screen.Camera.route) {
                    navController.navigate(Screen.Camera.route)
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("기록") },
            selected = currentDestination == Screen.History.route,
            onClick = {
                if (currentDestination != Screen.History.route) {
                    navController.navigate(Screen.History.route)
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Forum, contentDescription = null) },
            label = { Text("커뮤니티") },
            selected = currentDestination == Screen.Community.route,
            onClick = {
                if (currentDestination != Screen.Community.route) {
                    navController.navigate(Screen.Community.route)
                }
            }
        )
    }
}

@Composable
fun ResultItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
