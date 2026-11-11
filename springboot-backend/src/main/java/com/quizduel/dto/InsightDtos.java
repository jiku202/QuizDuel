package com.quizduel.dto;

import java.util.List;

public final class InsightDtos {
    private InsightDtos() {}

    public record TopicMastery(String topic, int percent) {}

    public record Stats(int activeBattles, int students, int averageScore, int sections) {}

    public record Dashboard(Stats stats, List<TopicMastery> topicMastery, List<BattleDtos.BattleRow> todaysBattles) {}

    public record Analytics(int averageScore, int battlesToday, int activeStudents, List<TopicMastery> topicMastery) {}

    public record LeaderboardEntry(int rank, long studentId, String name, int points, int streak, int badges) {}

    public record CreateBadgeRequest(String name, String description) {}
}
