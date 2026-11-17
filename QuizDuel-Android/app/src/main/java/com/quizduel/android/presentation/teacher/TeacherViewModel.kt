package com.quizduel.android.presentation.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.data.model.*
import com.quizduel.android.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherUiState(
    val isLoading: Boolean = false,
    val dashboard: DashboardResponse? = null,
    val analytics: AnalyticsResponse? = null,
    val sections: List<SectionDto> = emptyList(),
    val selectedSectionId: Long? = null,
    val todaysBattles: List<BattleRow> = emptyList(),
    val battleLog: List<BattleRow> = emptyList(),
    val selectedSectionRoster: List<RosterEntry> = emptyList(),
    val isRosterLoading: Boolean = false,
    val isGeneratingBattles: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class TeacherViewModel(
    private val authRepository: AuthRepository,
    private val sectionRepository: SectionRepository,
    private val battleRepository: BattleRepository,
    private val insightRepository: InsightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherUiState())
    val uiState: StateFlow<TeacherUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val dashResult = insightRepository.getDashboard()
            val sectionsResult = sectionRepository.getSections()
            val battlesResult = battleRepository.getTodaysBattles()
            val logResult = battleRepository.getBattleLog()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                dashboard = dashResult.getOrNull(),
                sections = sectionsResult.getOrNull() ?: emptyList(),
                todaysBattles = battlesResult.getOrNull() ?: emptyList(),
                battleLog = logResult.getOrNull() ?: emptyList()
            )
        }
    }

    fun loadAnalytics(sectionId: String? = null) {
        viewModelScope.launch {
            when (val result = insightRepository.getAnalytics(sectionId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(analytics = result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun generateBattles(sectionId: Long, time: String, weekend: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingBattles = true, errorMessage = null, successMessage = null)
            when (val result = battleRepository.generateBattles(sectionId, time, weekend)) {
                is NetworkResult.Success -> {
                    val res = result.data
                    val msg = if (res.alreadyExisted) {
                        "Today's battles already generated for this section."
                    } else {
                        "Created ${res.created} battle(s)! Topic: ${res.topic ?: "Selected topic"}"
                    }
                    _uiState.value = _uiState.value.copy(
                        isGeneratingBattles = false,
                        successMessage = msg
                    )
                    loadInitialData()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingBattles = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun createSection(name: String, grade: String, subject: String, emails: List<String>?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val request = CreateSectionRequest(name, grade, subject, emails)
            when (val result = sectionRepository.createSection(request)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Section '${result.data.name}' created! Join code: ${result.data.joinCode}"
                    )
                    loadInitialData()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun updateSection(id: Long, name: String?, battleTime: String?, weekendBattles: Boolean?) {
        viewModelScope.launch {
            val req = UpdateSectionRequest(name = name, battleTime = battleTime, weekendBattles = weekendBattles)
            when (val result = sectionRepository.updateSection(id, req)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Section updated successfully")
                    loadInitialData()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteSection(id: Long) {
        viewModelScope.launch {
            when (val result = sectionRepository.deleteSection(id)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Section deleted")
                    loadInitialData()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun loadRoster(sectionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedSectionId = sectionId, isRosterLoading = true)
            when (val result = sectionRepository.getRoster(sectionId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRosterLoading = false,
                        selectedSectionRoster = result.data
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRosterLoading = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun addStudentsToRoster(sectionId: Long, emails: List<String>) {
        viewModelScope.launch {
            when (val result = sectionRepository.addMembers(sectionId, emails)) {
                is NetworkResult.Success -> {
                    val addRes = result.data
                    val msg = buildString {
                        if (addRes.added.isNotEmpty()) append("Added ${addRes.added.size} student(s). ")
                        if (addRes.alreadyIn.isNotEmpty()) append("Already in section: ${addRes.alreadyIn.size}. ")
                        if (addRes.notFound.isNotEmpty()) append("No student account found for: ${addRes.notFound.joinToString()}.")
                    }
                    _uiState.value = _uiState.value.copy(successMessage = msg)
                    loadRoster(sectionId)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun removeStudentFromRoster(sectionId: Long, studentId: Long) {
        viewModelScope.launch {
            when (val result = sectionRepository.removeMember(sectionId, studentId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Student removed from class")
                    loadRoster(sectionId)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val sectionRepository: SectionRepository,
        private val battleRepository: BattleRepository,
        private val insightRepository: InsightRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeacherViewModel(authRepository, sectionRepository, battleRepository, insightRepository) as T
        }
    }
}
