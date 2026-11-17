package com.quizduel.android.presentation.student

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizduel.android.core.theme.*
import com.quizduel.android.data.model.BattleQuestion
import com.quizduel.android.presentation.common.ErrorStateView
import com.quizduel.android.presentation.common.LoadingView
import com.quizduel.android.presentation.common.NoticeBanner
import com.quizduel.android.presentation.common.NoticeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPlayScreen(
    viewModel: QuizPlayViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.battle?.topic ?: "Quiz Battle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    LoadingView(message = "Loading battle questions...")
                }
                state.errorMessage != null && state.battle == null -> {
                    ErrorStateView(
                        message = state.errorMessage ?: "Failed to load battle",
                        onRetry = { viewModel.loadBattle() }
                    )
                }
                state.isFinished -> {
                    QuizFinishedView(
                        state = state,
                        onBackToDashboard = onNavigateBack
                    )
                }
                state.isSubmitting -> {
                    LoadingView(message = "Submitting answers & calculating scores...")
                }
                state.currentQuestion != null -> {
                    QuizQuestionView(
                        state = state,
                        onSelectAnswer = { choice -> viewModel.selectAnswer(choice) }
                    )
                }
                else -> {
                    ErrorStateView(
                        message = "No questions found for this battle.",
                        onRetry = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
fun QuizQuestionView(
    state: QuizPlayUiState,
    onSelectAnswer: (String) -> Unit
) {
    val question = state.currentQuestion ?: return
    val total = state.totalQuestions
    val current = state.currentQuestionIndex + 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Header: Progress & Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $current of $total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
                // Timer Chip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (state.timeRemainingSeconds <= 5) ErrorRedLight else Slate100,
                    border = BorderStroke(
                        1.dp,
                        if (state.timeRemainingSeconds <= 5) ErrorRed else Slate300
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (state.timeRemainingSeconds <= 5) ErrorRed else Slate700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.timeRemainingSeconds}s",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (state.timeRemainingSeconds <= 5) ErrorRed else Slate900
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = Slate200
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Question Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = question.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Option Buttons (A, B, C, D)
            val choices = listOf("A", "B", "C", "D")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                choices.forEach { key ->
                    val text = question.choices[key] ?: ""
                    ChoiceOptionButton(
                        key = key,
                        text = text,
                        onClick = { onSelectAnswer(key) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChoiceOptionButton(
    key: String,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.5.dp, Slate300)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Slate100,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = key,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate800
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Slate900
            )
        }
    }
}

@Composable
fun QuizFinishedView(
    state: QuizPlayUiState,
    onBackToDashboard: () -> Unit
) {
    val battle = state.battle
    val score = state.submitResult?.score ?: battle?.player1Score ?: 0
    val total = state.submitResult?.total ?: battle?.questions?.size ?: 0
    val winner = state.submitResult?.battle?.winner ?: battle?.winner

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Trophy / Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryBlue,
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Battle Complete!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (winner != null) "Winner: $winner" else "Results submitted",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate300
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Slate800
                    ) {
                        Text(
                            text = "Score: $score / $total",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // Review of Questions (if available)
        if (battle != null && battle.questions.isNotEmpty()) {
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Question Review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
            }

            items(battle.questions) { q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = q.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                        if (q.correctAnswer != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val answerText = q.choices[q.correctAnswer] ?: ""
                            Text(
                                text = "Correct Answer: (${q.correctAnswer}) $answerText",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onBackToDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Back to Dashboard", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
