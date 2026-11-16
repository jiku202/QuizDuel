package com.quizduel.dto;

import java.util.List;

public final class InsightDtos {
    private InsightDtos() {}

    public record TopicMastery(String topic, int percent) {}

    public record Stats(int activeBattles, int students, int averageScore, int sections) {}

    public record Dashboard(Stats stats, List<TopicMastery> topicMastery, List<BattleDtos.BattleRow> todaysBattles) {}

    public record Analytics(int averageScore, int battlesToday, int activeStudents, List<TopicMastery> topicMastery) {}

    /** One row per student per section: a student in two sections appears twice, each with that section's points. */
    public record LeaderboardEntry(int position, long studentId, String name, long sectionId, String sectionName,
                                   int points, int streak, String rank) {}
}
