package com.example.capskin.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
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
import com.example.capskin.model.HistoryRepository
import com.example.capskin.model.SkinAnalysisResult
import com.example.capskin.navigation.Screen
import com.example.capskin.ui.components.ResultItem
import com.example.capskin.viewmodel.SkinViewModel

@Composable
fun ResultScreen(navController: NavHostController, viewModel: SkinViewModel) {
    val result = viewModel.analysisResult
    
    if (result == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("분석 결과를 불러오는 중...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        ResultContent(navController, result, viewModel.errorMessage)
    }
}

@Composable
fun ResultContent(navController: NavHostController, result: SkinAnalysisResult, errorMessage: String? = null) {
    val scrollState = rememberScrollState()

    // Base64 디코딩 최적화: key가 바뀔 때만 수행
    val originalBitmap = remember(result.originalImageBase64) { result.originalImageBase64.decodeBase64() }
    val melaninBitmap = remember(result.melaninImageBase64) { result.melaninImageBase64.decodeBase64() }
    val hemoglobinBitmap = remember(result.hemoglobinImageBase64) { result.hemoglobinImageBase64.decodeBase64() }

    // 리포트 파싱 최적화
    val analysisCards = remember(result.analysisReport) {
        result.analysisReport.split("\n\n").filter { it.isNotBlank() }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("알림") },
            text = { Text(errorMessage) },
            confirmButton = { Button(onClick = { }) { Text("확인") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
    ) {
        Text("정밀 분석 결과", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("분광 분석 데이터를 통해 산출된 리포트입니다.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(20.dp))

        // 촬영된 원본 사진 표시 (1:1)
        originalBitmap?.let {
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Original Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        ResultItem("피부 판정 타입", result.skinType, MaterialTheme.colorScheme.primary)
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        
        // 분석 이미지 시각화 섹션
        if (melaninBitmap != null || hemoglobinBitmap != null) {
            Text("AI 피부 성분 지도", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("피부 아래 성분 분포를 시각화한 결과입니다.", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                melaninBitmap?.let { AnalysisImageCard("멜라닌 분포", "색소 침착 및 잡티 밀집도", it) }
                hemoglobinBitmap?.let { AnalysisImageCard("헤모글로빈 분포", "혈관 확장 및 민감도", it) }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Text("AI 부위별 케어 가이드", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (analysisCards.isEmpty() || result.analysisReport == "분석 결과가 없습니다.") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    "현재 상태에서는 특별히 도드라진 피부 고민이 발견되지 않았습니다. 자외선 차단과 수분 공급 위주의 기본 케어를 유지해 주세요.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        } else {
            analysisCards.forEach { cardText ->
                val parts = cardText.split(" | ")
                if (parts.size >= 3) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = parts[0],
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = parts[1],
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = parts[2],
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { 
                navController.navigate(Screen.CreatePost.createRoute(result.melaninLevel, result.hemoglobinLevel, result.skinType))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("분석 결과 커뮤니티에 공유하기")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = { navController.navigate(Screen.Home.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("확인")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AnalysisImageCard(title: String, description: String, bitmap: Bitmap) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = title,
                modifier = Modifier.fillMaxWidth().height(280.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun HistoryScreen(navController: NavHostController, viewModel: SkinViewModel) {
    val historyList = HistoryRepository.history
    
    if (historyList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("아직 분석 기록이 없습니다.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("최근 분석 기록", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(historyList, key = { it.hashCode() }) { result -> // key 추가로 성능 개선
                HistoryItem(result) {
                    viewModel.selectResult(result)
                    navController.navigate(Screen.Result.route)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(result: SkinAnalysisResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(result.skinType, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("AI 정밀 리포트 보기", fontSize = 13.sp, color = Color.Gray)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        }
    }
}

// 확장 함수로 분리하여 가독성 및 재사용성 향상
private fun String?.decodeBase64(): Bitmap? {
    if (this.isNullOrBlank()) return null
    return try {
        val imageBytes = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        null
    }
}
