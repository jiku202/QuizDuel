package com.quizduel.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public final class BattleDtos {
    private BattleDtos() {}

    /**
     * battleTime = "HH:mm" and weekendBattles are chosen by the teacher when generating; both are optional and
     * default to the section's saved settings. Whatever is chosen is saved on the section.
     */
    public record GenerateRequest(Long sectionId, String battleTime, Boolean weekendBattles) {}

    /** topic is omitted when battles already existed for today. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GenerateResult(int created, boolean alreadyExisted, String date, String topic) {}

    public record AnswerItem(Long questionId, String selected, Double timeTaken) {}

    public record SubmitAnswersRequest(List<AnswerItem> answers) {}

    /** One row of the battle tables. winner/result/topic may be null. */
    public record BattleRow(long id, long sectionId, String date, String topic, String status, String statusLabel,
                            String result, int player1Score, int player2Score, String player1, String player2,
                            String winner, String battleTime) {}

    public record SubmitResponse(int score, int total, BattleRow battle) {}
}
