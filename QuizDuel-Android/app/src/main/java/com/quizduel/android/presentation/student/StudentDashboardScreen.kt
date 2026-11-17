package com.quizduel.android.presentation.student

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizduel.android.core.theme.*
import com.quizduel.android.data.model.BattleRow
import com.quizduel.android.data.model.SectionDto
import com.quizduel.android.presentation.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: StudentViewModel,
    onNavigateToBattles: () -> Unit,
    onNavigateToPlayBattle: (battleId: Long) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showJoinDialog by remember { mutableStateOf(false) }
    var joinCodeInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryBlue,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = (state.user?.name?.firstOrNull() ?: 'S').toString().uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = state.user?.name ?: "Student",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.user?.school ?: "Mabini Elementary School",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLeaderboard) {
                        Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard", tint = Slate700)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Settings", tint = Slate700)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    joinCodeInput = ""
                    showJoinDialog = true
                },
                containerColor = Slate900,
                contentColor = PureWhite,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Join Class", fontWeight = FontWeight.SemiBold) }
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
                Spacer(modifier = Modifier.height(4.dp))
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

            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Points",
                        value = "${state.user?.totalPoints ?: 0}",
                        icon = Icons.Default.EmojiEvents,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Streak",
                        value = "${state.user?.streak ?: 0}d",
                        icon = Icons.Default.LocalFireDepartment,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Classes",
                        value = "${state.sections.size}",
                        icon = Icons.Default.Class,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Today's Battle Banner / Highlight
            item {
                val pendingBattle = state.todaysBattles.firstOrNull { it.isPending || it.isInProgress }
                if (pendingBattle != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPlayBattle(pendingBattle.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PrimaryBlue,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "TODAY'S BATTLE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = pendingBattle.battleTime ?: "Daily",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate300
                                )
                            }
                            Text(
                                text = pendingBattle.topic ?: "Daily Quiz Battle",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "vs ${pendingBattle.player2 ?: "Practice Bot"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate300
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onNavigateToPlayBattle(pendingBattle.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = Slate900),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Battle Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Today's Battles Section
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
                        Text("View All", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (state.todaysBattles.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Battles Today Yet",
                        subtitle = "Your teacher will generate today's match. Check back soon!",
                        icon = Icons.Default.SportsEsports
                    )
                }
            } else {
                items(state.todaysBattles.take(3)) { battle ->
                    StudentBattleCard(
                        battle = battle,
                        onPlayClick = { onNavigateToPlayBattle(battle.id) }
                    )
                }
            }

            // Joined Sections Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Enrolled Classes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
            }

            if (state.sections.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "Not in any classes yet",
                        subtitle = "Tap 'Join Class' below and enter the join code provided by your teacher.",
                        icon = Icons.Default.School
                    )
                }
            } else {
                items(state.sections) { section ->
                    StudentSectionCard(section = section)
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Join Section Dialog
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Join a Class", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter the 6-character class code given by your teacher (e.g. MABINI):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = joinCodeInput,
                        onValueChange = { joinCodeInput = it.uppercase() },
                        label = { Text("Class Join Code") },
                        placeholder = { Text("MABINI") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.joinSection(joinCodeInput)
                        showJoinDialog = false
                    },
                    enabled = joinCodeInput.isNotBlank() && !state.isJoiningSection,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text("Join")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentBattleCard(
    battle: BattleRow,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = battle.topic ?: "Daily Match",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${battle.player1} vs ${battle.player2 ?: "Practice Bot"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when {
                            battle.isCompleted -> SuccessGreenLight
                            battle.isInProgress -> WarningAmberLight
                            else -> Slate100
                        }
                    ) {
                        Text(
                            text = battle.statusLabel ?: battle.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                battle.isCompleted -> SuccessGreen
                                battle.isInProgress -> WarningAmber
                                else -> Slate700
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (battle.isCompleted && battle.winner != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Winner: ${battle.winner}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }
                }
            }

            if (battle.isCompleted) {
                OutlinedButton(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Score: ${battle.player1Score}–${battle.player2Score}", fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Play", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun StudentSectionCard(section: SectionDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Slate100
                ) {
                    Text(
                        text = "CODE: ${section.joinCode}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${section.gradeLevel} • ${section.subject}",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Battle Time: ${section.battleTime} ${if (section.weekendBattles) "(Weekend Active)" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate600
            )
        }
    }
}
