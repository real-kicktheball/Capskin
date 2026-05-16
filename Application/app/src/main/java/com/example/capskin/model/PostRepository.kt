package com.example.capskin.model

import androidx.compose.runtime.mutableStateListOf

object PostRepository {
    private val _posts = mutableStateListOf(
        Post(
            id = "1", 
            title = "지성 피부 관리 꿀팁 공유합니다!", 
            content = "요즘 날씨가 더워지면서 피지 분비가 늘어나는데, 제가 효과 본 루틴이에요. 첫 번째로는 클렌징 오일 대신 워터를 사용하는 거예요. 두 번째는 수분 크림을 얇게 여러 번 레이어링 하는 거죠. 여러분은 어떻게 관리하시나요?", 
            author = "꿀피부관리사", 
            time = "10분 전", 
            likes = 24, 
            dislikes = 2, 
            comments = listOf(
                Comment("c1", "건성러", "오 클렌징 워터도 좋군요!", "5분 전"),
                Comment("c2", "지성탈출", "레이어링 팁 감사합니다", "3분 전")
            ),
            melaninLevel = 0.3f, 
            hemoglobinLevel = 0.25f, 
            skinType = "지성"
        ),
        Post(
            id = "2", 
            title = "CapSkin 분석 결과 어떤가요?", 
            content = "멜라닌 농도가 좀 높게 나왔는데 자외선 차단제 추천 부탁드려요.", 
            author = "스킨뉴비", 
            time = "30분 전", 
            likes = 12, 
            dislikes = 0, 
            comments = listOf(
                Comment("c3", "선크림박사", "무기자차 위주로 알아보세요", "20분 전")
            ),
            melaninLevel = 0.65f, 
            hemoglobinLevel = 0.4f, 
            skinType = "건성"
        )
    )

    val posts: List<Post> get() = _posts

    fun addPost(post: Post) {
        _posts.add(0, post)
    }

    fun toggleLike(postId: String) {
        val index = _posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = _posts[index]
            val newIsLiked = !post.isLiked
            val newLikes = if (newIsLiked) post.likes + 1 else post.likes - 1
            
            // If liking, remove dislike
            var newIsDisliked = post.isDisliked
            var newDislikes = post.dislikes
            if (newIsLiked && post.isDisliked) {
                newIsDisliked = false
                newDislikes -= 1
            }

            _posts[index] = post.copy(
                isLiked = newIsLiked, 
                likes = newLikes,
                isDisliked = newIsDisliked,
                dislikes = newDislikes
            )
        }
    }

    fun toggleDislike(postId: String) {
        val index = _posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = _posts[index]
            val newIsDisliked = !post.isDisliked
            val newDislikes = if (newIsDisliked) post.dislikes + 1 else post.dislikes - 1
            
            // If disliking, remove like
            var newIsLiked = post.isLiked
            var newLikes = post.likes
            if (newIsDisliked && post.isLiked) {
                newIsLiked = false
                newLikes -= 1
            }

            _posts[index] = post.copy(
                isDisliked = newIsDisliked, 
                dislikes = newDislikes,
                isLiked = newIsLiked,
                likes = newLikes
            )
        }
    }

    fun addComment(postId: String, content: String, author: String = "사용자") {
        val index = _posts.indexOfFirst { it.id == postId }
        if (index != -1) {
            val post = _posts[index]
            val newComment = Comment(
                id = System.currentTimeMillis().toString(),
                author = author,
                content = content,
                time = "방금 전"
            )
            _posts[index] = post.copy(comments = post.comments + newComment)
        }
    }
    
    fun getPostById(postId: String?): Post? {
        return _posts.find { it.id == postId }
    }
}
