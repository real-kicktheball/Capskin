package com.example.capskin.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Result : Screen("result")
    object History : Screen("history")
}
