package com.example.capskin.model

import androidx.compose.runtime.mutableStateListOf

object PostRepository {
    private val _posts = mutableStateListOf(
        Post("1", "지성 피부 관리 꿀팁 공유합니다!", "요즘 날씨가 더워지면서 피지 분비가 늘어나는데, 제가 효과 본 루틴이에요. 첫 번째로는 클렌징 오일 대신 워터를 사용하는 거예요. 두 번째는 수분 크림을 얇게 여러 번 레이어링 하는 거죠. 여러분은 어떻게 관리하시나요?", "꿀피부관리사", "10분 전", 24, 2, 3, 0.3f, 0.25f, "지성"),
        Post("2", "CapSkin 분석 결과 어떤가요?", "멜라닌 농도가 좀 높게 나왔는데 자외선 차단제 추천 부탁드려요.", "스킨뉴비", "30분 전", 12, 0, 5, 0.65f, 0.4f, "건성"),
        Post("3", "오늘의 스킨케어 인증샷", "오늘 피부 컨디션 최고네요! 다들 물 많이 드세요.", "물마시기왕", "1시간 전", 45, 1, 12, 0.2f, 0.15f, "복합성"),
        Post("4", "민감성 피부 화장품 추천", "성분이 순한 수분 크림 찾고 있어요. 다들 뭐 쓰시나요?", "민감러", "2시간 전", 8, 3, 15, 0.4f, 0.55f, "민감성")
    )

    val posts: List<Post> get() = _posts

    fun addPost(post: Post) {
        _posts.add(0, post) // Add to top
    }
}
