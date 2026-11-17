package com.quizduel.android.data.model

data class QuestionRequest(
    val sectionId: Long,
    val topic: String,
    val text: String,
    val choices: Map<String, String>,
    val correctAnswer: String,
    val difficulty: String = "Easy"
)

data class QuestionDto(
    val id: Long,
    val sectionId: Long,
    val topic: String,
    val text: String,
    val choices: Map<String, String>,
    val correctAnswer: String,
    val difficulty: String = "Easy"
)

data class TopicRequest(
    val sectionId: Long? = null,
    val name: String
)

data class CopyTopicRequest(
    val sectionId: Long,
    val copyQuestions: Boolean = true
)

data class TopicDto(
    val id: Long,
    val sectionId: Long,
    val sectionName: String? = null,
    val name: String,
    val questionCount: Int = 0,
    val createdAt: String? = null
)
