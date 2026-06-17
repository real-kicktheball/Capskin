package com.example.capskin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.capskin.model.Post
import com.example.capskin.model.PostRepository
import com.example.capskin.viewmodel.SkinViewModel
import java.util.UUID
import android.util.Base64
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavHostController, 
    melanin: Float?, 
    hemoglobin: Float?, 
    skinType: String?,
    viewModel: SkinViewModel
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val analysisResult = viewModel.analysisResult

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("글쓰기", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { 
                            val newPost = Post(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                content = content,
                                author = "사용자",
                                time = "방금 전",
                                likes = 0,
                                dislikes = 0,
                                melaninLevel = melanin,
                                hemoglobinLevel = hemoglobin,
                                skinType = skinType,
                                analysisReport = analysisResult?.analysisReport, // 리포트 전체 저장
                                melaninImageBase64 = analysisResult?.melaninImageBase64,
                                hemoglobinImageBase64 = analysisResult?.hemoglobinImageBase64,
                                originalImageBase64 = analysisResult?.originalImageBase64,
                                imageUrl = if (analysisResult?.originalImageBase64 != null) "attached_image" else null
                            )
                            PostRepository.addPost(newPost)
                            navController.popBackStack() 
                        },
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Text("완료", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Attached Analysis Info
            if (skinType != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("분석 결과가 첨부되었습니다", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    "피부 타입: $skinType",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // 첨부된 실제 사진 미리보기
                        if (analysisResult?.originalImageBase64 != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val bitmap = remember {
                                val imageBytes = Base64.decode(analysisResult.originalImageBase64, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            }
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("제목을 입력하세요", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { /* Focus move is automatic for Next */ })
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("피부 고민이나 관리 팁을 공유해 주세요.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
            )
        }
    }
}
