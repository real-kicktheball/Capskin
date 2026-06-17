package com.example.capskin.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capskin.model.HistoryRepository
import com.example.capskin.model.SkinAnalysisResult
import com.example.capskin.network.SkinApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class SkinViewModel : ViewModel() {
    private val apiService by lazy { SkinApiService.create() }

    var analysisResult by mutableStateOf<SkinAnalysisResult?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val zoneMap = mapOf(
        "cheek_left" to "왼쪽 볼",
        "cheek_right" to "오른쪽 볼",
        "nose" to "코",
        "forehead" to "이마",
        "mouth_chin" to "입가·턱"
    )

    fun selectResult(result: SkinAnalysisResult) {
        analysisResult = result
    }

    fun analyzeSkin(context: Context, imageUri: Uri, onComplete: () -> Unit) {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1. 이미지 최적화 처리 (IO 쓰레드)
                val processedBitmap = withContext(Dispatchers.IO) {
                    processAndCropImage(context, imageUri)
                } ?: throw Exception("이미지를 불러올 수 없습니다.")
                
                val file = withContext(Dispatchers.IO) {
                    bitmapToFile(context, processedBitmap)
                }
                
                val originalBase64 = withContext(Dispatchers.Default) {
                    bitmapToBase64(processedBitmap)
                }

                // 2. 서버 전송
                val mediaType = MediaType.parse("image/jpeg")
                val requestFile = RequestBody.create(mediaType, file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = apiService.uploadPicture("https://dm89dn7dvhyoe.cloudfront.net/picture", body)
                
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    
                    val reportText = data.cards?.joinToString("\n\n") { card ->
                        val zoneName = zoneMap[card.zone] ?: card.zone
                        "$zoneName | ${card.concern} | ${card.care}"
                    } ?: "분석 결과가 없습니다."

                    val result = SkinAnalysisResult(
                        melaninLevel = data.melaninLevel ?: 0f,
                        hemoglobinLevel = data.hemoglobinLevel ?: 0f,
                        itaValue = data.itaValue ?: 0f,
                        skinType = data.skinType ?: "분석 완료",
                        analysisReport = reportText,
                        melaninImageBase64 = data.melaninMap,
                        hemoglobinImageBase64 = data.hemoglobinMap,
                        originalImageBase64 = originalBase64
                    )
                    
                    analysisResult = result
                    HistoryRepository.addResult(result)
                    onComplete()
                } else if (response.code() == 422) {
                    errorMessage = "얼굴을 인식하지 못했습니다. 얼굴이 가이드 원 안에 잘 들어오도록 다시 찍어주세요."
                } else {
                    errorMessage = "서버 분석 실패 (에러코드: ${response.code()})"
                }
                
                // 메모리 해제
                processedBitmap.recycle()
                
            } catch (e: Exception) {
                errorMessage = "오류 발생: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun processAndCropImage(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            
            // EXIF 회전 정보 보정
            val orientation = context.contentResolver.openInputStream(uri)?.use { exifStream ->
                ExifInterface(exifStream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> matrix.postRotate(90f) // 사용자 요청: 기본적으로 90도 회전
            }

            val rotatedBitmap = Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
            )
            
            if (rotatedBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            
            // 1:1 중앙 크롭
            val width = rotatedBitmap.width
            val height = rotatedBitmap.height
            val size = if (width > height) height else width
            val x = (width - size) / 2
            val y = (height - size) / 2
            
            val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, x, y, size, size)
            if (croppedBitmap != rotatedBitmap) {
                rotatedBitmap.recycle()
            }
            
            croppedBitmap
        }
    }

    private fun bitmapToFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) // 압축률 살짝 조정하여 용량 최적화
        }
        return file
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out) // Base64용은 더 압축
            Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
        }
    }
}
