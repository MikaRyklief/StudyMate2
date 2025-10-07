package com.example.studymate2.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface StudyApiService {
    @GET("todos")
    suspend fun fetchStudyTodos(@Query("userId") userId: Int = 1): List<StudyTodoResponse>

    companion object {
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

        fun create(): StudyApiService {
            val client = OkHttpClient.Builder().build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(StudyApiService::class.java)
        }
    }
}

data class StudyTodoResponse(
    val id: Int,
    val title: String,
    val completed: Boolean
)
