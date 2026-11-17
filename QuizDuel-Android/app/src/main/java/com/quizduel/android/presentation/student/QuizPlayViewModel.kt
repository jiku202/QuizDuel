package com.quizduel.android.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.data.model.*
import com.quizduel.android.data.repository.BattleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizPlayUiState(
    val isLoading: Boolean = true,
    val battle: BattleDetail? = null,
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: MutableMap<Long, String> = mutableMapOf(),
    val questionTimes: MutableMap<Long, Double> = mutableMapOf(),
    val timeRemainingSeconds: Int = 20,
    val isSubmitting: Boolean = false,
    val submitResult: SubmitResponse? = null,
    val errorMessage: String? = null,
    val isFinished: Boolean = false
) {
    val currentQuestion: BattleQuestion?
        get() = battle?.questions?.getOrNull(currentQuestionIndex)

    val totalQuestions: Int
        get() = battle?.questions?.size ?: 0

    val progress: Float
        get() = if (totalQuestions > 0) (currentQuestionIndex + 1).toFloat() / totalQuestions else 0f
}

class QuizPlayViewModel(
    private val battleId: Long,
    private val battleRepository: BattleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizPlayUiState())
    val uiState: StateFlow<QuizPlayUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var questionStartTime: Long = System.currentTimeMillis()

    init {
        loadBattle()
    }

    fun loadBattle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = battleRepository.getBattleDetail(battleId)) {
                is NetworkResult.Success -> {
                    val detail = result.data
                    val alreadyCompleted = detail.isCompleted
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        battle = detail,
                        isFinished = alreadyCompleted
                    )
                    if (!alreadyCompleted && detail.questions.isNotEmpty()) {
                        startTimer()
                    }
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

    private fun startTimer() {
        timerJob?.cancel()
        questionStartTime = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(timeRemainingSeconds = 20)

        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    timeRemainingSeconds = _uiState.value.timeRemainingSeconds - 1
                )
            }
            // Time expired for this question -> auto-skip / record empty answer
            selectAnswer("")
        }
    }

    fun selectAnswer(choice: String) {
        val currentQ = _uiState.value.currentQuestion ?: return
        val timeTakenSeconds = ((System.currentTimeMillis() - questionStartTime) / 1000.0).coerceAtLeast(0.5)

        val updatedAnswers = HashMap(_uiState.value.selectedAnswers)
        val updatedTimes = HashMap(_uiState.value.questionTimes)

        if (choice.isNotBlank()) {
            updatedAnswers[currentQ.id] = choice
        }
        updatedTimes[currentQ.id] = timeTakenSeconds

        timerJob?.cancel()

        val nextIndex = _uiState.value.currentQuestionIndex + 1
        if (nextIndex < _uiState.value.totalQuestions) {
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswers = updatedAnswers,
                questionTimes = updatedTimes
            )
            startTimer()
        } else {
            // All questions answered, submit answers!
            _uiState.value = _uiState.value.copy(
                selectedAnswers = updatedAnswers,
                questionTimes = updatedTimes
            )
            submitFinalAnswers(updatedAnswers, updatedTimes)
        }
    }

    private fun submitFinalAnswers(answers: Map<Long, String>, times: Map<Long, Double>) {
        val battle = _uiState.value.battle ?: return
        val answerItems = battle.questions.map { q ->
            AnswerItem(
                questionId = q.id,
                selected = answers[q.id] ?: "A", // Default choice fallback if unanswered
                timeTaken = times[q.id] ?: 5.0
            )
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            when (val result = battleRepository.submitAnswers(battleId, answerItems)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        isFinished = true,
                        submitResult = result.data
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(
        private val battleId: Long,
        private val battleRepository: BattleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuizPlayViewModel(battleId, battleRepository) as T
        }
    }
}
