package com.quizduel.android.core.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quizduel.android.di.AppContainer
import com.quizduel.android.presentation.auth.AuthViewModel
import com.quizduel.android.presentation.auth.LoginScreen
import com.quizduel.android.presentation.auth.RegisterScreen
import com.quizduel.android.presentation.common.*
import com.quizduel.android.presentation.student.*
import com.quizduel.android.presentation.teacher.*

@Composable
fun QuizDuelNavGraph(
    navController: NavHostController,
    container: AppContainer
) {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(container.authRepository, container.tokenManager)
    )

    val savedUser = container.authRepository.getSavedUser()
    val hasToken = container.authRepository.hasToken()
    val startDestination = if (hasToken && savedUser != null) {
        if (savedUser.isTeacher) Screen.TeacherHome.route else Screen.StudentHome.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth: Login
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { isTeacher ->
                    val target = if (isTeacher) Screen.TeacherHome.route else Screen.StudentHome.route
                    navController.navigate(target) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Auth: Register
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { isTeacher ->
                    val target = if (isTeacher) Screen.TeacherHome.route else Screen.StudentHome.route
                    navController.navigate(target) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Student Dashboard
        composable(Screen.StudentHome.route) {
            val studentViewModel: StudentViewModel = viewModel(
                factory = StudentViewModel.Factory(
                    container.authRepository,
                    container.sectionRepository,
                    container.battleRepository
                )
            )
            StudentDashboardScreen(
                viewModel = studentViewModel,
                onNavigateToBattles = {
                    navController.navigate(Screen.StudentBattles.route)
                },
                onNavigateToPlayBattle = { battleId ->
                    navController.navigate(Screen.QuizPlay.createRoute(battleId))
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Student Battles List
        composable(Screen.StudentBattles.route) {
            val studentViewModel: StudentViewModel = viewModel(
                factory = StudentViewModel.Factory(
                    container.authRepository,
                    container.sectionRepository,
                    container.battleRepository
                )
            )
            StudentBattlesScreen(
                viewModel = studentViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayBattle = { battleId ->
                    navController.navigate(Screen.QuizPlay.createRoute(battleId))
                }
            )
        }

        // Student Quiz Player
        composable(
            route = Screen.QuizPlay.route,
            arguments = listOf(navArgument("battleId") { type = NavType.LongType })
        ) { backStackEntry ->
            val battleId = backStackEntry.arguments?.getLong("battleId") ?: 0L
            val quizViewModel: QuizPlayViewModel = viewModel(
                factory = QuizPlayViewModel.Factory(battleId, container.battleRepository)
            )
            QuizPlayScreen(
                viewModel = quizViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Teacher Dashboard
        composable(Screen.TeacherHome.route) {
            val teacherViewModel: TeacherViewModel = viewModel(
                factory = TeacherViewModel.Factory(
                    container.authRepository,
                    container.sectionRepository,
                    container.battleRepository,
                    container.insightRepository
                )
            )
            TeacherDashboardScreen(
                viewModel = teacherViewModel,
                onNavigateToSections = { navController.navigate(Screen.Sections.route) },
                onNavigateToQuestions = { navController.navigate(Screen.QuestionBank.route) },
                onNavigateToBattles = { navController.navigate(Screen.TeacherBattles.route) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // Teacher Sections
        composable(Screen.Sections.route) {
            val teacherViewModel: TeacherViewModel = viewModel(
                factory = TeacherViewModel.Factory(
                    container.authRepository,
                    container.sectionRepository,
                    container.battleRepository,
                    container.insightRepository
                )
            )
            SectionsScreen(
                viewModel = teacherViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Teacher Question Bank
        composable(Screen.QuestionBank.route) {
            val questionViewModel: QuestionBankViewModel = viewModel(
                factory = QuestionBankViewModel.Factory(
                    container.questionRepository,
                    container.sectionRepository
                )
            )
            QuestionBankScreen(
                viewModel = questionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Teacher Battles Management
        composable(Screen.TeacherBattles.route) {
            val teacherViewModel: TeacherViewModel = viewModel(
                factory = TeacherViewModel.Factory(
                    container.authRepository,
                    container.sectionRepository,
                    container.battleRepository,
                    container.insightRepository
                )
            )
            TeacherBattlesScreen(
                viewModel = teacherViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Teacher Analytics
        composable(Screen.Analytics.route) {
            val teacherViewModel: TeacherViewModel = viewModel(
                factory = TeacherViewModel.Factory(
                    container.authRepository,
                    container.sectionRepository,
                    container.battleRepository,
                    container.insightRepository
                )
            )
            AnalyticsScreen(
                viewModel = teacherViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Shared: Leaderboard
        composable(Screen.Leaderboard.route) {
            val commonViewModel: CommonViewModel = viewModel(
                factory = CommonViewModel.Factory(
                    container.authRepository,
                    container.sectionRepository,
                    container.insightRepository,
                    container.tokenManager
                )
            )
            LeaderboardScreen(
                viewModel = commonViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Shared: Settings
        composable(Screen.Settings.route) {
            val commonViewModel: CommonViewModel = viewModel(
                factory = CommonViewModel.Factory(
                    container.authRepository,
                    container.sectionRepository,
                    container.insightRepository,
                    container.tokenManager
                )
            )
            SettingsScreen(
                viewModel = commonViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
