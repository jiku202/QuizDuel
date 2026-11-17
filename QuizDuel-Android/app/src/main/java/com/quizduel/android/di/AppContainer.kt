package com.quizduel.android.di

import android.content.Context
import com.quizduel.android.core.network.ApiClient
import com.quizduel.android.core.security.TokenManager
import com.quizduel.android.data.repository.*

class AppContainer(context: Context) {

    val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    val apiClient: ApiClient by lazy {
        ApiClient(tokenManager)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiClient, tokenManager)
    }

    val sectionRepository: SectionRepository by lazy {
        SectionRepository(apiClient)
    }

    val questionRepository: QuestionRepository by lazy {
        QuestionRepository(apiClient)
    }

    val battleRepository: BattleRepository by lazy {
        BattleRepository(apiClient)
    }

    val insightRepository: InsightRepository by lazy {
        InsightRepository(apiClient)
    }
}
