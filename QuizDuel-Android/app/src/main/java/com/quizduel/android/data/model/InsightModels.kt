package com.quizduel.android.data.model

data class TopicMastery(
    val topic: String,
    val percent: Int
)

data class Stats(
    val activeBattles: Int = 0,
    val students: Int = 0,
    val averageScore: Int = 0,
    val sections: Int = 0
)

data class DashboardResponse(
    val stats: Stats,
    val topicMastery: List<TopicMastery> = emptyList(),
    val todaysBattles: List<BattleRow> = emptyList()
)

data class AnalyticsResponse(
    val averageScore: Int = 0,
    val battlesToday: Int = 0,
    val activeStudents: Int = 0,
    val topicMastery: List<TopicMastery> = emptyList()
)

data class LeaderboardEntry(
    val position: Int,
    val studentId: Long,
    val name: String,
    val sectionId: Long,
    val sectionName: String,
    val points: Int,
    val streak: Int,
    val rank: String? = null
)
