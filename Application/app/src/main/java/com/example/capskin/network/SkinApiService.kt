package com.example.capskin.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface SkinApiService {
    @Multipart
    @POST
    suspend fun uploadPicture(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): Response<SkinAnalysisResponse>

    companion object {
        private const val BASE_URL = "https://dm89dn7dvhyoe.cloudfront.net/"

        fun create(): SkinApiService {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SkinApiService::class.java)
        }
    }
}
