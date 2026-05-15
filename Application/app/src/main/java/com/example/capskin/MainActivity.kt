package com.example.capskin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.capskin.navigation.Screen
import com.example.capskin.ui.components.BottomNavigationBar
import com.example.capskin.ui.screens.CameraScreen
import com.example.capskin.ui.screens.HistoryScreen
import com.example.capskin.ui.screens.HomeScreen
import com.example.capskin.ui.screens.ResultScreen
import com.example.capskin.ui.theme.CapSkinTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CapSkinTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Camera.route) { CameraScreen(navController) }
            composable(Screen.Result.route) { ResultScreen(navController) }
            composable(Screen.History.route) { HistoryScreen() }
        }
    }
}
