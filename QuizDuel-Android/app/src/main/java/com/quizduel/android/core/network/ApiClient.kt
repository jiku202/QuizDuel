package com.quizduel.android.core.network

import com.quizduel.android.core.security.TokenManager
import com.quizduel.android.data.remote.QuizDuelApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(private val tokenManager: TokenManager) {

    private var currentBaseUrl: String = tokenManager.getBaseUrl()
    private var cachedService: QuizDuelApiService? = null

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun getService(): QuizDuelApiService {
        val configuredUrl = tokenManager.getBaseUrl()
        if (cachedService != null && configuredUrl == currentBaseUrl) {
            return cachedService!!
        }

        currentBaseUrl = configuredUrl
        val retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(QuizDuelApiService::class.java)
        cachedService = service
        return service
    }
}
