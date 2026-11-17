package com.quizduel.android

import android.app.Application
import com.quizduel.android.di.AppContainer

class QuizDuelApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
