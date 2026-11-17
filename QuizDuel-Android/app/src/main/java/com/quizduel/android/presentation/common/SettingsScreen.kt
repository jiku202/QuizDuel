package com.quizduel.android.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizduel.android.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CommonViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user

    var serverUrlInput by remember(state.baseUrl) { mutableStateOf(state.baseUrl) }
    var selectedSectionId by remember { mutableStateOf<Long?>(null) }
    var battleTimeInput by remember { mutableStateOf("15:00") }
    var allowWeekendInput by remember { mutableStateOf(false) }

    LaunchedEffect(state.sections) {
        if (selectedSectionId == null && state.sections.isNotEmpty()) {
            val first = state.sections.first()
            selectedSectionId = first.id
            battleTimeInput = first.battleTime
            allowWeekendInput = first.weekendBattles
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            // Profile Card
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
                            text = "Account Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        RoleBadge(role = user?.role ?: "student")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user?.name ?: "User",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = user?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500
                    )
                    if (!user?.school.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "School: ${user?.school}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Total Points: ${user?.totalPoints ?: 0} • Streak: ${user?.streak ?: 0} days",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
            }

            // Teacher Section Settings (if Teacher)
            if (user?.isTeacher == true && state.sections.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Class Battle Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Section Picker
                        Text("Select Section:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            state.sections.forEach { sec ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = selectedSectionId == sec.id,
                                        onClick = {
                                            selectedSectionId = sec.id
                                            battleTimeInput = sec.battleTime
                                            allowWeekendInput = sec.weekendBattles
                                        }
                                    )
                                    Text(sec.name, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = battleTimeInput,
                            onValueChange = { battleTimeInput = it },
                            label = { Text("Daily Battle Time (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = allowWeekendInput,
                                onCheckedChange = { allowWeekendInput = it }
                            )
                            Text("Allow weekend battles", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                selectedSectionId?.let { secId ->
                                    viewModel.updateSectionSettings(secId, battleTimeInput, allowWeekendInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Class Settings")
                        }
                    }
                }
            }

            // Server Address Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Backend Server Connection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Android emulator accesses host computer at http://10.0.2.2:5000/api/",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("API Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.updateBaseUrl(serverUrlInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply URL")
                    }
                }
            }

            // Logout Button
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                border = BorderStroke(1.5.dp, ErrorRed)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
