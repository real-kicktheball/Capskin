package com.example.capskin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.capskin.model.PostRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavHostController, postId: String?) {
    val post = PostRepository.getPostById(postId) ?: return
    var commentText by remember { mutableStateOf("") }
    
    val decodeBase64: (String?) -> android.graphics.Bitmap? = { base64String ->
        try {
            if (!base64String.isNullOrBlank()) {
                val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    val originalBitmap = remember(post.originalImageBase64) { decodeBase64(post.originalImageBase64) }
    val melaninBitmap = remember(post.melaninImageBase64) { decodeBase64(post.melaninImageBase64) }
    val hemoglobinBitmap = remember(post.hemoglobinImageBase64) { decodeBase64(post.hemoglobinImageBase64) }

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
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("댓글을 입력하세요") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                PostRepository.addComment(post.id, commentText)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.author.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(post.author, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(post.time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(post.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                
                if (post.skinType != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = post.skinType,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(post.content, fontSize = 16.sp, lineHeight = 24.sp)

                // 1. 실제 촬영 사진 표시
                if (originalBitmap != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("첨부된 분석 사진", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Image(
                            bitmap = originalBitmap.asImageBitmap(),
                            contentDescription = "Original Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // 2. 분석 이미지 시각화 섹션 (멜라닌/헤모글로빈 지도)
                if (melaninBitmap != null || hemoglobinBitmap != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("피부 분석 데이터", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        melaninBitmap?.let {
                            Column(modifier = Modifier.weight(1f)) {
                                Card(shape = RoundedCornerShape(8.dp)) {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(150.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text("멜라닌 지도", fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp))
                            }
                        }
                        hemoglobinBitmap?.let {
                            Column(modifier = Modifier.weight(1f)) {
                                Card(shape = RoundedCornerShape(8.dp)) {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(150.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text("헤모글로빈 지도", fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp))
                            }
                        }
                    }
                }

                // 3. AI 맞춤형 분석 리포트 표시
                post.analysisReport?.let { report ->
                    val cards = report.split("\n\n").filter { it.isNotBlank() }
                    if (cards.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("AI 분석 리포트", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        cards.forEach { cardText ->
                            val parts = cardText.split(" | ")
                            if (parts.size >= 3) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = parts[0],
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(parts[1], fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(parts[2], fontSize = 13.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InteractionButton(
                        icon = if (post.isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        count = post.likes,
                        color = if (post.isLiked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { PostRepository.toggleLike(post.id) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    InteractionButton(
                        icon = if (post.isDisliked) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                        count = post.dislikes,
                        color = if (post.isDisliked) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { PostRepository.toggleDislike(post.id) }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("댓글 ${post.commentCount}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(post.comments) { comment ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(comment.time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(comment.content, fontSize = 14.sp)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
