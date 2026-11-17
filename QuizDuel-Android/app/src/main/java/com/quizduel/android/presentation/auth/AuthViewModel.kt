package com.quizduel.android.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.core.security.TokenManager
import com.quizduel.android.data.model.LoginRequest
import com.quizduel.android.data.model.RegisterRequest
import com.quizduel.android.data.model.UserDto
import com.quizduel.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: UserDto? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val baseUrl: String = TokenManager.DEFAULT_EMULATOR_BASE_URL
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(
            user = authRepository.getSavedUser(),
            isAuthenticated = authRepository.hasToken() && authRepository.getSavedUser() != null,
            baseUrl = tokenManager.getBaseUrl()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        if (!authRepository.hasToken()) {
            _uiState.value = _uiState.value.copy(isAuthenticated = false, user = null)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = authRepository.getMe()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.data,
                        errorMessage = null
                    )
                }
                is NetworkResult.Error -> {
                    authRepository.logout()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        user = null,
                        errorMessage = null // Silent fallback to login screen
                    )
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email and password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            when (val result = authRepository.login(LoginRequest(email.trim(), pass))) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.data.user,
                        errorMessage = null
                    )
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

    fun register(name: String, email: String, pass: String, school: String?, role: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Name, email, and password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val request = RegisterRequest(
                name = name.trim(),
                email = email.trim(),
                password = pass,
                school = school?.trim()?.ifBlank { null },
                role = role
            )
            when (val result = authRepository.register(request)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.data.user,
                        successMessage = "Account created successfully!"
                    )
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

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState(
            isAuthenticated = false,
            user = null,
            baseUrl = tokenManager.getBaseUrl()
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun updateBaseUrl(newUrl: String) {
        tokenManager.setBaseUrl(newUrl)
        _uiState.value = _uiState.value.copy(baseUrl = tokenManager.getBaseUrl())
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val tokenManager: TokenManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository, tokenManager) as T
        }
    }
}
