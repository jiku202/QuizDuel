package com.quizduel.dto;

import java.util.Map;

public final class QuestionDtos {
    private QuestionDtos() {}

    /** choices is a map with keys "A".."D". Used for both create and (partial) update. */
    public record QuestionRequest(Long sectionId, String topic, String text, Map<String, String> choices,
                                  String correctAnswer, String difficulty) {}

    public record QuestionDto(long id, long sectionId, String topic, String text, Map<String, String> choices,
                              String correctAnswer, String difficulty) {}
}
