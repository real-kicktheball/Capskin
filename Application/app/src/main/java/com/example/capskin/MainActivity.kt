package com.example.capskin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.capskin.navigation.Screen
import com.example.capskin.ui.components.BottomNavigationBar
import com.example.capskin.ui.screens.*
import com.example.capskin.ui.theme.CapSkinTheme
import com.example.capskin.viewmodel.SkinViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

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
    val skinViewModel: SkinViewModel = viewModel()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination?.route
            if (currentDestination != Screen.Splash.route) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Community.route) { CommunityScreen(navController) }
            composable(
                route = Screen.PostDetail.route,
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                PostDetailScreen(navController, backStackEntry.arguments?.getString("postId"))
            }
            composable(
                route = Screen.CreatePost.route,
                arguments = listOf(
                    navArgument("melanin") { 
                        type = NavType.FloatType
                        defaultValue = -1f
                    },
                    navArgument("hemoglobin") { 
                        type = NavType.FloatType
                        defaultValue = -1f
                    },
                    navArgument("skinType") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val melanin = backStackEntry.arguments?.getFloat("melanin").takeIf { it != -1f }
                val hemoglobin = backStackEntry.arguments?.getFloat("hemoglobin").takeIf { it != -1f }
                val skinType = backStackEntry.arguments?.getString("skinType")
                CreatePostScreen(navController, melanin, hemoglobin, skinType, skinViewModel)
            }
            composable(Screen.Camera.route) { CameraScreen(navController, skinViewModel) }
            composable(Screen.Result.route) { ResultScreen(navController, skinViewModel) }
            composable(Screen.History.route) { HistoryScreen(navController, skinViewModel) }
        }
    }
}
