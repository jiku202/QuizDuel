package com.quizduel.android.core.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Student screens
    data object StudentHome : Screen("student_home")
    data object StudentBattles : Screen("student_battles")
    data object QuizPlay : Screen("quiz_play/{battleId}") {
        fun createRoute(battleId: Long) = "quiz_play/$battleId"
    }

    // Teacher screens
    data object TeacherHome : Screen("teacher_home")
    data object Sections : Screen("sections")
    data object QuestionBank : Screen("question_bank")
    data object TeacherBattles : Screen("teacher_battles")
    data object Analytics : Screen("analytics")

    // Shared screens
    data object Leaderboard : Screen("leaderboard")
    data object Settings : Screen("settings")
}
