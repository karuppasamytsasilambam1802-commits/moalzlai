package com.example.myapplication.network

import com.example.myapplication.data.models.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET("/health")
    suspend fun checkHealth(): Response<Map<String, String>>

    @POST("/api/rooms")
    suspend fun createRoom(@Body request: CreateRoomRequest): Response<Room>

    @POST("/api/rooms/join")
    suspend fun joinRoom(@Body request: JoinRoomRequest): Response<Room>

    @GET("/api/rooms/{room_id}")
    suspend fun getRoom(@Path("room_id") roomId: String): Response<Room>

    @POST("/api/translate")
    suspend fun translate(@Body request: TranslationRequest): Response<TranslationResponse>

    @POST("/api/auth/github")
    suspend fun authenticateGitHub(@Body request: GitHubAuthRequest): Response<GitHubAuthResponse>

    @GET("/api/rooms/{room_id}/messages")
    suspend fun getMessages(@Path("room_id") roomId: String): Response<List<Message>>

    @POST("/api/rooms/{room_id}/leave")
    suspend fun leaveRoom(
        @Path("room_id") roomId: String,
        @Query("participant_id") participantId: String
    ): Response<Map<String, String>>
}

object RetrofitClient {
    private var instance: ApiService? = null
    private var lastBaseUrl: String = ""

    fun getService(): ApiService {
        val currentUrl = NetworkConfig.getServerUrl() + "/"
        if (instance == null || lastBaseUrl != currentUrl) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            instance = Retrofit.Builder()
                .baseUrl(currentUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)

            lastBaseUrl = currentUrl
        }
        return instance!!
    }
}
