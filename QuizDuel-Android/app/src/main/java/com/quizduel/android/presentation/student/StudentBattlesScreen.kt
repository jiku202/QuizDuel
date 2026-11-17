package com.quizduel.android.presentation.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizduel.android.core.theme.PureWhite
import com.quizduel.android.core.theme.Slate50
import com.quizduel.android.presentation.common.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentBattlesScreen(
    viewModel: StudentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayBattle: (battleId: Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var filter by remember { mutableStateOf("all") } // "all", "pending", "completed"

    val filteredBattles = remember(state.todaysBattles, filter) {
        when (filter) {
            "pending" -> state.todaysBattles.filter { !it.isCompleted }
            "completed" -> state.todaysBattles.filter { it.isCompleted }
            else -> state.todaysBattles
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Battles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
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
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == "all",
                    onClick = { filter = "all" },
                    label = { Text("All (${state.todaysBattles.size})") }
                )
                FilterChip(
                    selected = filter == "pending",
                    onClick = { filter = "pending" },
                    label = { Text("Pending (${state.todaysBattles.count { !it.isCompleted }})") }
                )
                FilterChip(
                    selected = filter == "completed",
                    onClick = { filter = "completed" },
                    label = { Text("Completed (${state.todaysBattles.count { it.isCompleted }})") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredBattles.isEmpty()) {
                EmptyStateView(
                    title = "No Battles Found",
                    subtitle = if (filter == "all") "No battles scheduled for today." else "No battles matching filter '$filter'."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredBattles) { battle ->
                        StudentBattleCard(
                            battle = battle,
                            onPlayClick = { onNavigateToPlayBattle(battle.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
