package com.quizduel.android.presentation.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.quizduel.android.data.model.QuestionDto
import com.quizduel.android.data.model.QuestionRequest
import com.quizduel.android.data.model.TopicDto
import com.quizduel.android.presentation.common.EmptyStateView
import com.quizduel.android.presentation.common.NoticeBanner
import com.quizduel.android.presentation.common.NoticeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    viewModel: QuestionBankViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var showCreateTopicDialog by remember { mutableStateOf(false) }
    var newTopicName by remember { mutableStateOf("") }
    var showQuestionDialog by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<QuestionDto?>(null) }
    var deletingQuestion by remember { mutableStateOf<QuestionDto?>(null) }

    // Question form state
    var qText by remember { mutableStateOf("") }
    var qChoiceA by remember { mutableStateOf("") }
    var qChoiceB by remember { mutableStateOf("") }
    var qChoiceC by remember { mutableStateOf("") }
    var qChoiceD by remember { mutableStateOf("") }
    var qCorrectAnswer by remember { mutableStateOf("A") }
    var qDifficulty by remember { mutableStateOf("Easy") }

    fun openQuestionEditor(q: QuestionDto? = null) {
        editingQuestion = q
        if (q != null) {
            qText = q.text
            qChoiceA = q.choices["A"] ?: ""
            qChoiceB = q.choices["B"] ?: ""
            qChoiceC = q.choices["C"] ?: ""
            qChoiceD = q.choices["D"] ?: ""
            qCorrectAnswer = q.correctAnswer
            qDifficulty = q.difficulty
        } else {
            qText = ""
            qChoiceA = ""
            qChoiceB = ""
            qChoiceC = ""
            qChoiceD = ""
            qCorrectAnswer = "A"
            qDifficulty = "Easy"
        }
        showQuestionDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question Bank", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadSectionsAndTopics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate50)
            )
        },
        floatingActionButton = {
            if (state.selectedTopic != null) {
                ExtendedFloatingActionButton(
                    onClick = { openQuestionEditor(null) },
                    containerColor = Slate900,
                    contentColor = PureWhite,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Question", fontWeight = FontWeight.SemiBold) }
                )
            }
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

            // Section Filter Dropdown / Row
            if (state.sections.isNotEmpty()) {
                var sectionMenuOpen by remember { mutableStateOf(false) }
                val currentSection = state.sections.find { it.id == state.selectedSectionId }

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sectionMenuOpen = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentSection?.name ?: "All Sections",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = sectionMenuOpen,
                        onDismissRequest = { sectionMenuOpen = false }
                    ) {
                        state.sections.forEach { sec ->
                            DropdownMenuItem(
                                text = { Text(sec.name) },
                                onClick = {
                                    viewModel.selectSection(sec.id)
                                    sectionMenuOpen = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Topics Filter Bar (Horizontal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.topics.forEach { topic ->
                    val isSelected = state.selectedTopic?.id == topic.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectTopic(topic) },
                        label = {
                            Text("${topic.name} (${topic.questionCount})", fontWeight = FontWeight.SemiBold)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate900,
                            selectedLabelColor = PureWhite
                        )
                    )
                }

                // Add topic chip
                AssistChip(
                    onClick = {
                        newTopicName = ""
                        showCreateTopicDialog = true
                    },
                    label = { Text("+ Add Topic") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Questions List
            if (state.selectedTopic == null) {
                EmptyStateView(
                    title = "Select or Create a Topic",
                    subtitle = "Select a topic above to manage its question pool.",
                    icon = Icons.Default.Quiz
                )
            } else if (state.questions.isEmpty()) {
                EmptyStateView(
                    title = "No Questions in '${state.selectedTopic?.name}'",
                    subtitle = "Tap '+ New Question' below to add your first question to this topic.",
                    icon = Icons.Default.HelpOutline
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.questions) { q ->
                        QuestionItemCard(
                            question = q,
                            onEdit = { openQuestionEditor(q) },
                            onDelete = { deletingQuestion = q }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Create Topic Dialog
    if (showCreateTopicDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTopicDialog = false },
            title = { Text("Create Topic", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newTopicName,
                    onValueChange = { newTopicName = it },
                    label = { Text("Topic Name (e.g. Filipino Heroes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val secId = state.selectedSectionId ?: state.sections.firstOrNull()?.id
                        if (secId != null && newTopicName.isNotBlank()) {
                            viewModel.createTopic(secId, newTopicName)
                            showCreateTopicDialog = false
                        }
                    },
                    enabled = newTopicName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTopicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add / Edit Question Dialog
    if (showQuestionDialog && state.selectedTopic != null) {
        val topic = state.selectedTopic!!
        AlertDialog(
            onDismissRequest = { showQuestionDialog = false },
            title = {
                Text(
                    if (editingQuestion != null) "Edit Question" else "New Question",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = qText,
                        onValueChange = { qText = it },
                        label = { Text("Question Text") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = qChoiceA,
                        onValueChange = { qChoiceA = it },
                        label = { Text("Choice A") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qChoiceB,
                        onValueChange = { qChoiceB = it },
                        label = { Text("Choice B") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qChoiceC,
                        onValueChange = { qChoiceC = it },
                        label = { Text("Choice C") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qChoiceD,
                        onValueChange = { qChoiceD = it },
                        label = { Text("Choice D") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Correct Answer Selector
                    Text("Correct Answer:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("A", "B", "C", "D").forEach { ans ->
                            FilterChip(
                                selected = qCorrectAnswer == ans,
                                onClick = { qCorrectAnswer = ans },
                                label = { Text(ans) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SuccessGreen,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }

                    // Difficulty Selector
                    Text("Difficulty:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Easy", "Medium", "Hard").forEach { diff ->
                            FilterChip(
                                selected = qDifficulty == diff,
                                onClick = { qDifficulty = diff },
                                label = { Text(diff) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate800,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val choices = mapOf(
                            "A" to qChoiceA,
                            "B" to qChoiceB,
                            "C" to qChoiceC,
                            "D" to qChoiceD
                        )
                        val req = QuestionRequest(
                            sectionId = topic.sectionId,
                            topic = topic.name,
                            text = qText,
                            choices = choices,
                            correctAnswer = qCorrectAnswer,
                            difficulty = qDifficulty
                        )
                        if (editingQuestion != null) {
                            viewModel.updateQuestion(editingQuestion!!.id, req)
                        } else {
                            viewModel.createQuestion(req)
                        }
                        showQuestionDialog = false
                    },
                    enabled = qText.isNotBlank() && qChoiceA.isNotBlank() && qChoiceB.isNotBlank() && qChoiceC.isNotBlank() && qChoiceD.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text(if (editingQuestion != null) "Save Changes" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuestionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Question Confirm
    deletingQuestion?.let { q ->
        AlertDialog(
            onDismissRequest = { deletingQuestion = null },
            title = { Text("Delete Question", fontWeight = FontWeight.Bold) },
            text = { Text("Delete this question: '${q.text}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteQuestion(q.id)
                        deletingQuestion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingQuestion = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun QuestionItemCard(
    question: QuestionDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (question.difficulty) {
                        "Hard" -> ErrorRedLight
                        "Medium" -> WarningAmberLight
                        else -> SuccessGreenLight
                    }
                ) {
                    Text(
                        text = question.difficulty.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (question.difficulty) {
                            "Hard" -> ErrorRed
                            "Medium" -> WarningAmber
                            else -> SuccessGreen
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Slate900
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Choices preview
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("A", "B", "C", "D").forEach { key ->
                    val isCorrect = question.correctAnswer == key
                    val text = question.choices[key] ?: ""
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$key: $text",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCorrect) SuccessGreen else Slate600
                        )
                        if (isCorrect) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Correct",
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
