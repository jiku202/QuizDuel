package com.quizduel.android.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.data.model.BattleRow
import com.quizduel.android.data.model.SectionDto
import com.quizduel.android.data.model.UserDto
import com.quizduel.android.data.repository.AuthRepository
import com.quizduel.android.data.repository.BattleRepository
import com.quizduel.android.data.repository.SectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val sections: List<SectionDto> = emptyList(),
    val todaysBattles: List<BattleRow> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isJoiningSection: Boolean = false
)

class StudentViewModel(
    private val authRepository: AuthRepository,
    private val sectionRepository: SectionRepository,
    private val battleRepository: BattleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentUiState(user = authRepository.getSavedUser()))
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Refresh user profile
            val userResult = authRepository.getMe()
            val currentUser = userResult.getOrNull() ?: authRepository.getSavedUser()

            // Fetch joined sections
            val sectionsResult = sectionRepository.getSections()
            val sectionsList = sectionsResult.getOrNull() ?: emptyList()

            // Fetch today's battles
            val battlesResult = battleRepository.getMyTodaysBattles()
            val battlesList = battlesResult.getOrNull() ?: emptyList()

            val errorMsg = when {
                sectionsResult is NetworkResult.Error -> sectionsResult.message
                battlesResult is NetworkResult.Error -> battlesResult.message
                else -> null
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                user = currentUser,
                sections = sectionsList,
                todaysBattles = battlesList,
                errorMessage = errorMsg
            )
        }
    }

    fun joinSection(joinCode: String) {
        if (joinCode.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Join code cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isJoiningSection = true, errorMessage = null, successMessage = null)
            when (val result = sectionRepository.joinSection(joinCode.trim())) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isJoiningSection = false,
                        successMessage = "Joined ${result.data.section.name} successfully!"
                    )
                    loadData()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isJoiningSection = false,
                        errorMessage = result.message
                    )
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
        private val battleRepository: BattleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentViewModel(authRepository, sectionRepository, battleRepository) as T
        }
    }
}
