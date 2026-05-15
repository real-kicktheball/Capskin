package com.example.capskin.model

data class Post(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val time: String,
    val likes: Int,
    val dislikes: Int,
    val commentCount: Int
)
