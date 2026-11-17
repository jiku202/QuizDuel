package com.quizduel.android.presentation.teacher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizduel.android.core.theme.*
import com.quizduel.android.data.model.RosterEntry
import com.quizduel.android.data.model.SectionDto
import com.quizduel.android.presentation.common.EmptyStateView
import com.quizduel.android.presentation.common.NoticeBanner
import com.quizduel.android.presentation.common.NoticeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionsScreen(
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<SectionDto?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<SectionDto?>(null) }
    var viewingRosterSection by remember { mutableStateOf<SectionDto?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var studentToRemove by remember { mutableStateOf<RosterEntry?>(null) }

    // Create form states
    var newName by remember { mutableStateOf("") }
    var newGrade by remember { mutableStateOf("Grade 3") }
    var newSubject by remember { mutableStateOf("Araling Panlipunan") }
    var renameInput by remember { mutableStateOf("") }
    var addEmailsInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sections & Classes", fontWeight = FontWeight.Bold) },
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
                onClick = {
                    newName = ""
                    newGrade = "Grade 3"
                    newSubject = "Araling Panlipunan"
                    showCreateDialog = true
                },
                containerColor = Slate900,
                contentColor = PureWhite,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Section", fontWeight = FontWeight.SemiBold) }
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

            if (state.sections.isEmpty()) {
                EmptyStateView(
                    title = "No Sections Yet",
                    subtitle = "Create your first section using the button below to generate join codes and manage students.",
                    icon = Icons.Default.Class
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.sections) { sec ->
                        SectionTeacherCard(
                            section = sec,
                            onCopyCode = { code ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Join Code", code)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Join code $code copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onViewRoster = {
                                viewingRosterSection = sec
                                viewModel.loadRoster(sec.id)
                            },
                            onRename = {
                                renameInput = sec.name
                                showRenameDialog = sec
                            },
                            onDelete = {
                                showDeleteConfirm = sec
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Create Section Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Section", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Section Name (e.g. Grade 3 - Mabini)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newGrade,
                        onValueChange = { newGrade = it },
                        label = { Text("Grade Level") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSubject,
                        onValueChange = { newSubject = it },
                        label = { Text("Subject") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.createSection(newName, newGrade, newSubject, null)
                            showCreateDialog = false
                        }
                    },
                    enabled = newName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Dialog
    showRenameDialog?.let { sec ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Section", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.updateSection(sec.id, renameInput, null, null)
                            showRenameDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirm
    showDeleteConfirm?.let { sec ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Section", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${sec.name}'? This will also remove all questions and battles linked to this section.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSection(sec.id)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Roster BottomSheet / Dialog
    viewingRosterSection?.let { sec ->
        AlertDialog(
            onDismissRequest = { viewingRosterSection = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${sec.name} Roster", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = {
                        addEmailsInput = ""
                        showAddMemberDialog = true
                    }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Student", tint = Slate800)
                    }
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    val roster = state.selectedSectionRoster
                    if (state.isRosterLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (roster.isEmpty()) {
                        Text(
                            "No students enrolled yet. Share join code '${sec.joinCode}' or tap the add button above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(roster) { student ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Slate100,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text(student.email, style = MaterialTheme.typography.labelSmall, color = Slate500)
                                            Text(
                                                "${student.points} pts • ${student.streak}d streak • ${student.rank ?: "Newcomer"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PrimaryBlue
                                            )
                                        }
                                        IconButton(onClick = { studentToRemove = student }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = ErrorRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewingRosterSection = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Member Dialog
    if (showAddMemberDialog && viewingRosterSection != null) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Students by Email", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter registered student email addresses separated by commas or spaces:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = addEmailsInput,
                        onValueChange = { addEmailsInput = it },
                        label = { Text("Student Emails") },
                        placeholder = { Text("student1@school.edu.ph, student2@...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val emails = addEmailsInput.split(",", ";", " ", "\n")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        if (emails.isNotEmpty()) {
                            viewModel.addStudentsToRoster(viewingRosterSection!!.id, emails)
                            showAddMemberDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Remove Student Confirm
    studentToRemove?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToRemove = null },
            title = { Text("Remove Student", fontWeight = FontWeight.Bold) },
            text = { Text("Remove '${student.name}' from this class?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewingRosterSection?.let { sec ->
                            viewModel.removeStudentFromRoster(sec.id, student.id)
                        }
                        studentToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Remove", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionTeacherCard(
    section: SectionDto,
    onCopyCode: (String) -> Unit,
    onViewRoster: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "${section.gradeLevel} • ${section.subject}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Roster / Students") },
                            onClick = {
                                menuExpanded = false
                                onViewRoster()
                            },
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                menuExpanded = false
                                onRename()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = ErrorRed) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Slate100,
                    modifier = Modifier.clickable { onCopyCode(section.joinCode) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JOIN CODE: ${section.joinCode}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp), tint = Slate600)
                    }
                }

                Text(
                    text = "${section.studentCount ?: 0} Students",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
            }
        }
    }
}
