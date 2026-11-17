package com.quizduel.android.presentation.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.quizduel.android.core.network.NetworkResult
import com.quizduel.android.data.model.*
import com.quizduel.android.data.repository.QuestionRepository
import com.quizduel.android.data.repository.SectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuestionBankUiState(
    val isLoading: Boolean = false,
    val sections: List<SectionDto> = emptyList(),
    val selectedSectionId: Long? = null,
    val topics: List<TopicDto> = emptyList(),
    val selectedTopic: TopicDto? = null,
    val questions: List<QuestionDto> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class QuestionBankViewModel(
    private val questionRepository: QuestionRepository,
    private val sectionRepository: SectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionBankUiState())
    val uiState: StateFlow<QuestionBankUiState> = _uiState.asStateFlow()

    init {
        loadSectionsAndTopics()
    }

    fun loadSectionsAndTopics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val secRes = sectionRepository.getSections()
            val sectionsList = secRes.getOrNull() ?: emptyList()
            val initialSecId = sectionsList.firstOrNull()?.id

            _uiState.value = _uiState.value.copy(
                sections = sectionsList,
                selectedSectionId = initialSecId
            )

            loadTopics(initialSecId?.toString())
        }
    }

    fun selectSection(sectionId: Long?) {
        _uiState.value = _uiState.value.copy(
            selectedSectionId = sectionId,
            selectedTopic = null,
            questions = emptyList()
        )
        loadTopics(sectionId?.toString())
    }

    fun selectTopic(topic: TopicDto?) {
        _uiState.value = _uiState.value.copy(selectedTopic = topic)
        if (topic != null) {
            loadQuestions(topic.sectionId.toString(), topic.name)
        } else {
            _uiState.value = _uiState.value.copy(questions = emptyList())
        }
    }

    private fun loadTopics(sectionId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = questionRepository.listTopics(sectionId)) {
                is NetworkResult.Success -> {
                    val topicsList = result.data
                    val firstTopic = topicsList.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        topics = topicsList,
                        selectedTopic = firstTopic
                    )
                    if (firstTopic != null) {
                        loadQuestions(firstTopic.sectionId.toString(), firstTopic.name)
                    } else {
                        _uiState.value = _uiState.value.copy(questions = emptyList())
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun loadQuestions(sectionId: String?, topic: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = questionRepository.getQuestions(sectionId, topic)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, questions = result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun createTopic(sectionId: Long, name: String) {
        viewModelScope.launch {
            when (val result = questionRepository.createTopic(sectionId, name.trim())) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Topic '${name}' created!")
                    loadTopics(_uiState.value.selectedSectionId?.toString())
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun renameTopic(topicId: Long, newName: String) {
        viewModelScope.launch {
            when (val result = questionRepository.renameTopic(topicId, newName.trim())) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Topic renamed!")
                    loadTopics(_uiState.value.selectedSectionId?.toString())
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteTopic(topicId: Long) {
        viewModelScope.launch {
            when (val result = questionRepository.deleteTopic(topicId)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Topic deleted")
                    loadTopics(_uiState.value.selectedSectionId?.toString())
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun createQuestion(req: QuestionRequest) {
        viewModelScope.launch {
            when (val result = questionRepository.createQuestion(req)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Question created!")
                    val currentTopic = _uiState.value.selectedTopic
                    if (currentTopic != null) {
                        loadQuestions(currentTopic.sectionId.toString(), currentTopic.name)
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun updateQuestion(id: Long, req: QuestionRequest) {
        viewModelScope.launch {
            when (val result = questionRepository.updateQuestion(id, req)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Question updated!")
                    val currentTopic = _uiState.value.selectedTopic
                    if (currentTopic != null) {
                        loadQuestions(currentTopic.sectionId.toString(), currentTopic.name)
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message)
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun deleteQuestion(id: Long) {
        viewModelScope.launch {
            when (val result = questionRepository.deleteQuestion(id)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(successMessage = "Question deleted")
                    val currentTopic = _uiState.value.selectedTopic
                    if (currentTopic != null) {
                        loadQuestions(currentTopic.sectionId.toString(), currentTopic.name)
                    }
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
        private val questionRepository: QuestionRepository,
        private val sectionRepository: SectionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuestionBankViewModel(questionRepository, sectionRepository) as T
        }
    }
}
