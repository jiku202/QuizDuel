package com.quizduel.android.presentation.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizduel.android.core.theme.*
import com.quizduel.android.data.model.BattleRow
import com.quizduel.android.presentation.common.EmptyStateView
import com.quizduel.android.presentation.common.NoticeBanner
import com.quizduel.android.presentation.common.NoticeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherBattlesScreen(
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var showGenerateDialog by remember { mutableStateOf(false) }
    var battleTimeInput by remember { mutableStateOf("15:00") }
    var allowWeekend by remember { mutableStateOf(false) }
    var selectedSectionForGen by remember { mutableStateOf<Long?>(null) }
    var tabIndex by remember { mutableStateOf(0) } // 0 = Today's Battles, 1 = Battle Results Log

    LaunchedEffect(state.sections) {
        if (selectedSectionForGen == null && state.sections.isNotEmpty()) {
            selectedSectionForGen = state.sections.first().id
            battleTimeInput = state.sections.first().battleTime
            allowWeekend = state.sections.first().weekendBattles
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battles Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadInitialData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showGenerateDialog = true },
                containerColor = Slate900,
                contentColor = PureWhite,
                icon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                text = { Text("Generate Battles", fontWeight = FontWeight.SemiBold) }
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (state.errorMessage != null) {
                NoticeBanner(
                    text = state.errorMessage,
                    type = NoticeType.ERROR,
                    onDismiss = { viewModel.clearMessages() },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (state.successMessage != null) {
                NoticeBanner(
                    text = state.successMessage,
                    type = NoticeType.SUCCESS,
                    onDismiss = { viewModel.clearMessages() },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Tabs: Today's Battles vs Battle Results Log
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = Slate50,
                contentColor = Slate900,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text("Today's Battles (${state.todaysBattles.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text("Results Log (${state.battleLog.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tabIndex == 0) {
                // Today's battles
                if (state.todaysBattles.isEmpty()) {
                    EmptyStateView(
                        title = "No Battles for Today",
                        subtitle = "Tap 'Generate Battles' below to automatically pair students and pick a topic.",
                        icon = Icons.Default.SportsEsports
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.todaysBattles) { b ->
                            TeacherBattleRowCard(b)
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            } else {
                // Historical Log
                if (state.battleLog.isEmpty()) {
                    EmptyStateView(
                        title = "No Completed Battles Yet",
                        subtitle = "When students finish playing today's battles, their results and winners will appear here.",
                        icon = Icons.Default.History
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.battleLog) { b ->
                            BattleLogRowCard(b)
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }

    // Generate Battles Dialog
    if (showGenerateDialog) {
        AlertDialog(
            onDismissRequest = { showGenerateDialog = false },
            title = { Text("Generate Today's Battles", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Pairs up enrolled students in the section at random for today's match. An odd student is paired with the Practice Bot.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    // Section Picker
                    if (state.sections.isNotEmpty()) {
                        Text("Section:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            state.sections.forEach { sec ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = selectedSectionForGen == sec.id,
                                        onClick = {
                                            selectedSectionForGen = sec.id
                                            battleTimeInput = sec.battleTime
                                            allowWeekend = sec.weekendBattles
                                        }
                                    )
                                    Text(sec.name, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = battleTimeInput,
                        onValueChange = { battleTimeInput = it },
                        label = { Text("Scheduled Time (HH:mm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = allowWeekend,
                            onCheckedChange = { allowWeekend = it }
                        )
                        Text("Allow weekend battles", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val secId = selectedSectionForGen ?: state.sections.firstOrNull()?.id
                        if (secId != null) {
                            viewModel.generateBattles(secId, battleTimeInput, allowWeekend)
                            showGenerateDialog = false
                        }
                    },
                    enabled = !state.isGeneratingBattles && state.sections.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text("Generate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGenerateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TeacherBattleRowCard(b: BattleRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = b.topic ?: "Daily Match",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (b.isCompleted) SuccessGreenLight else Slate100
                ) {
                    Text(
                        text = b.statusLabel ?: b.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (b.isCompleted) SuccessGreen else Slate700,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${b.player1} vs ${b.player2 ?: "Practice Bot"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate700
                )
                Text(
                    text = b.battleTime ?: "15:00",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
        }
    }
}

@Composable
fun BattleLogRowCard(b: BattleRow) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${b.player1} vs ${b.player2 ?: "Practice Bot"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "${b.date} • ${b.topic ?: "Battle"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
                if (b.winner != null) {
                    Text(
                        text = "Winner: ${b.winner}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SuccessGreen
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Slate100
            ) {
                Text(
                    text = "${b.player1Score} – ${b.player2Score}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Slate900,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
