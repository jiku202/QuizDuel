package com.quizduel.android.data.model

data class GenerateRequest(
    val sectionId: Long,
    val battleTime: String? = null,
    val weekendBattles: Boolean? = null
)

data class GenerateResult(
    val created: Int,
    val alreadyExisted: Boolean,
    val date: String,
    val topic: String? = null
)

data class AnswerItem(
    val questionId: Long,
    val selected: String,
    val timeTaken: Double = 0.0
)

data class SubmitAnswersRequest(
    val answers: List<AnswerItem>
)

data class BattleRow(
    val id: Long,
    val sectionId: Long,
    val date: String,
    val topic: String? = null,
    val status: String,
    val statusLabel: String? = null,
    val result: String? = null,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val player1: String? = null,
    val player2: String? = null,
    val winner: String? = null,
    val battleTime: String? = null
) {
    val isPending: Boolean get() = "pending".equals(status, ignoreCase = true)
    val isInProgress: Boolean get() = "in_progress".equals(status, ignoreCase = true)
    val isCompleted: Boolean get() = "completed".equals(status, ignoreCase = true)
}

data class BattleQuestion(
    val id: Long,
    val text: String,
    val choices: Map<String, String>,
    val correctAnswer: String? = null
)

data class BattleDetail(
    val id: Long,
    val sectionId: Long,
    val date: String,
    val topic: String? = null,
    val status: String,
    val statusLabel: String? = null,
    val result: String? = null,
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val player1: String? = null,
    val player2: String? = null,
    val winner: String? = null,
    val questions: List<BattleQuestion> = emptyList()
) {
    val isPending: Boolean get() = "pending".equals(status, ignoreCase = true)
    val isInProgress: Boolean get() = "in_progress".equals(status, ignoreCase = true)
    val isCompleted: Boolean get() = "completed".equals(status, ignoreCase = true)
}

data class SubmitResponse(
    val score: Int,
    val total: Int,
    val battle: BattleRow
)
