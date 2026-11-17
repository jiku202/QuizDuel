package com.quizduel.android.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.core.security.TokenManager
import com.quizduel.android.data.model.*
import com.quizduel.android.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CommonUiState(
    val isLoading: Boolean = false,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val badges: List<BadgeDto> = emptyList(),
    val sections: List<SectionDto> = emptyList(),
    val selectedSectionId: String? = null,
    val user: UserDto? = null,
    val baseUrl: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class CommonViewModel(
    private val authRepository: AuthRepository,
    private val sectionRepository: SectionRepository,
    private val insightRepository: InsightRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CommonUiState(
            user = authRepository.getSavedUser(),
            baseUrl = tokenManager.getBaseUrl()
        )
    )
    val uiState: StateFlow<CommonUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboardAndBadges()
    }

    fun loadLeaderboardAndBadges(sectionId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val secRes = sectionRepository.getSections()
            val secList = secRes.getOrNull() ?: emptyList()

            val leadRes = insightRepository.getLeaderboard(sectionId = sectionId)
            val badgeRes = insightRepository.getBadges()
            val userRes = authRepository.getMe()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                sections = secList,
                selectedSectionId = sectionId,
                leaderboard = leadRes.getOrNull() ?: emptyList(),
                badges = badgeRes.getOrNull() ?: emptyList(),
                user = userRes.getOrNull() ?: authRepository.getSavedUser()
            )
        }
    }

    fun updateSectionSettings(sectionId: Long, battleTime: String, weekend: Boolean) {
        viewModelScope.launch {
            val req = UpdateSectionRequest(battleTime = battleTime, weekendBattles = weekend)
            when (val res = sectionRepository.updateSection(sectionId, req)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Battle settings saved!")
                    loadLeaderboardAndBadges(_uiState.value.selectedSectionId)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = res.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun updateBaseUrl(newUrl: String) {
        tokenManager.setBaseUrl(newUrl)
        _uiState.value = _uiState.value.copy(baseUrl = tokenManager.getBaseUrl(), successMessage = "Server URL updated")
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val sectionRepository: SectionRepository,
        private val insightRepository: InsightRepository,
        private val tokenManager: TokenManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CommonViewModel(authRepository, sectionRepository, insightRepository, tokenManager) as T
        }
    }
}
