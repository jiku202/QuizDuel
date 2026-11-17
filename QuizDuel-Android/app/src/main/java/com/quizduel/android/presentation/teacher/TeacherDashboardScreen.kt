package com.quizduel.android.presentation.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizduel.android.core.theme.*
import com.quizduel.android.data.model.BattleRow
import com.quizduel.android.presentation.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherViewModel,
    onNavigateToSections: () -> Unit,
    onNavigateToQuestions: () -> Unit,
    onNavigateToBattles: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Slate900,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "T",
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Teacher Portal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "QuizDuel Dashboard",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadInitialData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Slate700)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Slate700)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                if (state.errorMessage != null) {
                    NoticeBanner(
                        text = state.errorMessage,
                        type = NoticeType.ERROR,
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
                if (state.successMessage != null) {
                    NoticeBanner(
                        text = state.successMessage,
                        type = NoticeType.SUCCESS,
                        onDismiss = { viewModel.clearMessages() }
                    )
                }
            }

            // Quick Nav Shortcuts
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Teacher Management",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NavIconTile("Sections", Icons.Default.Class, onNavigateToSections)
                            NavIconTile("Questions", Icons.Default.Quiz, onNavigateToQuestions)
                            NavIconTile("Battles", Icons.Default.SportsEsports, onNavigateToBattles)
                            NavIconTile("Analytics", Icons.Default.BarChart, onNavigateToAnalytics)
                            NavIconTile("Ranks", Icons.Default.Leaderboard, onNavigateToLeaderboard)
                        }
                    }
                }
            }

            // 4 Stats Overview
            item {
                val stats = state.dashboard?.stats
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Active Battles",
                            value = "${stats?.activeBattles ?: 0}",
                            icon = Icons.Default.SportsEsports,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Students",
                            value = "${stats?.students ?: 0}",
                            icon = Icons.Default.Group,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Avg Score",
                            value = "${stats?.averageScore ?: 0}%",
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Sections",
                            value = "${stats?.sections ?: state.sections.size}",
                            icon = Icons.Default.School,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Topic Mastery Section
            item {
                val masteryList = state.dashboard?.topicMastery ?: emptyList()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Topic Mastery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Icon(Icons.Default.Insights, contentDescription = null, tint = Slate400)
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        if (masteryList.isEmpty()) {
                            Text(
                                text = "No battle answers recorded yet. Mastery updates as students play daily matches.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate500
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                masteryList.forEach { item ->
                                    MasteryProgressBar(topic = item.topic, percent = item.percent)
                                }
                            }
                        }
                    }
                }
            }

            // Today's Battles Table / List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Battles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    TextButton(onClick = onNavigateToBattles) {
                        Text("Manage Battles", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            val battles = state.todaysBattles
            if (battles.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Battles Generated Today",
                        subtitle = "Go to the Battles section to generate today's match for your classes.",
                        icon = Icons.Default.SportsEsports
                    )
                }
            } else {
                items(battles.take(4)) { b ->
                    TeacherBattleItem(battle = b)
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun NavIconTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Slate100,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Slate800,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = Slate700
        )
    }
}

@Composable
fun TeacherBattleItem(battle: BattleRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${battle.player1} vs ${battle.player2 ?: "Practice Bot"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900
                )
                Text(
                    text = "${battle.topic ?: "General"} • ${battle.battleTime ?: "15:00"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (battle.isCompleted) SuccessGreenLight else Slate100
            ) {
                Text(
                    text = if (battle.isCompleted) "Completed (${battle.player1Score}–${battle.player2Score})" else (battle.statusLabel ?: "Pending"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (battle.isCompleted) SuccessGreen else Slate700,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
