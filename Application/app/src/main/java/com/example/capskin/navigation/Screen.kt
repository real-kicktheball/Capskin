package com.example.capskin.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Result : Screen("result")
    object History : Screen("history")
    object Community : Screen("community")
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
    object CreatePost : Screen("create_post?melanin={melanin}&hemoglobin={hemoglobin}&skinType={skinType}") {
        fun createRoute(melanin: Float, hemoglobin: Float, skinType: String) = 
            "create_post?melanin=$melanin&hemoglobin=$hemoglobin&skinType=$skinType"
    }
}
