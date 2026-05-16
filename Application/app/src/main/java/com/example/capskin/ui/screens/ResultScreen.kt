package com.example.capskin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.capskin.model.SkinAnalysisResult
import com.example.capskin.navigation.Screen
import com.example.capskin.ui.components.ResultItem

@Composable
fun ResultScreen(navController: NavHostController) {
    val result = remember {
        SkinAnalysisResult(
            melaninLevel = 0.45f,
            hemoglobinLevel = 0.32f,
            itaValue = 42.5f,
            skinType = "복합성 (혈관형 다크서클 주의)",
            analysisReport = "현재 고객님의 피부는 헤모글로빈 농도가 다소 높아 자극에 민감할 수 있습니다. 자외선 차단제를 필수로 사용하시고, 비타민 C 성분이 포함된 제품은 저녁 루틴에 추가하는 것을 권장합니다."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text("분석 결과", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        ResultItem("피부 타입", result.skinType, Color(0xFF6200EE))
        ResultItem("멜라닌 농도", "${(result.melaninLevel * 100).toInt()}%", Color.DarkGray)
        ResultItem("헤모글로빈 농도", "${(result.hemoglobinLevel * 100).toInt()}%", Color.Red)
        ResultItem("ITA 지수 (언더톤)", result.itaValue.toString(), Color.Magenta)

        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AI 맞춤형 가이드", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(result.analysisReport, fontSize = 15.sp, lineHeight = 22.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { 
                navController.navigate(
                    Screen.CreatePost.createRoute(
                        result.melaninLevel,
                        result.hemoglobinLevel,
                        result.skinType
                    )
                )
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
    }
}

@Composable
fun HistoryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("분석 기록이 준비 중입니다.")
    }
}
