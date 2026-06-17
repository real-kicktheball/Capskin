package com.example.capskin.model

data class Post(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val time: String,
    var likes: Int,
    var dislikes: Int,
    val comments: List<Comment> = emptyList(),
    val melaninLevel: Float? = null,
    val hemoglobinLevel: Float? = null,
    val skinType: String? = null,
    val analysisReport: String? = null, // 전체 분석 리포트 추가
    val imageUrl: String? = null,
    val melaninImageBase64: String? = null,
    val hemoglobinImageBase64: String? = null,
    val originalImageBase64: String? = null,
    var isLiked: Boolean = false,
    var isDisliked: Boolean = false
) {
    val commentCount: Int get() = comments.size
}

data class Comment(
    val id: String,
    val author: String,
    val content: String,
    val time: String
)
