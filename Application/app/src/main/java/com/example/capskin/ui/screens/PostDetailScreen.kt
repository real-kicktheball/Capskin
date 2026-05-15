package com.example.capskin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.capskin.model.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavHostController, postId: String?) {
    // In a real app, you would fetch the post by ID. Mocking for now.
    val post = remember {
        Post("1", "지성 피부 관리 꿀팁 공유합니다!", 
            "요즘 날씨가 더워지면서 피지 분비가 늘어나는데, 제가 효과 본 루틴이에요. 첫 번째로는 클렌징 오일 대신 워터를 사용하는 거예요. 두 번째는 수분 크림을 얇게 여러 번 레이어링 하는 거죠. 여러분은 어떻게 관리하시나요?", 
            "꿀피부관리사", "10분 전", 24, 2, 3, 0.3f, "지성")
    }

    val comments = remember {
        listOf(
            Comment("피부미인", "저도 워터로 바꾸고 좁쌀 많이 들어갔어요!", "5분 전"),
            Comment("관리왕", "레이어링 팁 감사합니다! 오늘부터 해봐야겠네요.", "3분 전"),
            Comment("스킨케어러버", "좋은 정보 감사합니다 ㅎㅎ", "방금")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            CommentInputField()
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                PostContent(post)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Text(
                    "댓글 ${post.commentCount}",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            items(comments) { comment ->
                CommentItem(comment)
            }
        }
    }
}

@Composable
fun PostContent(post: Post) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(post.author.take(1), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(post.author, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(post.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.weight(1f))
            if (post.skinType != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = post.skinType,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(post.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        if (post.melaninLevel != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "분석 데이터: 멜라닌 농도 ${(post.melaninLevel * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(post.content, fontSize = 16.sp, lineHeight = 24.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ThumbUpOffAlt, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            Text(" ${post.likes}", modifier = Modifier.padding(end = 16.dp))
            Icon(Icons.Default.ThumbDownOffAlt, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(20.dp))
            Text(" ${post.dislikes}")
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(comment.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(comment.content, fontSize = 14.sp)
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}

@Composable
fun CommentInputField() {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("댓글을 입력하세요...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

data class Comment(val author: String, val content: String, val time: String)
