package com.quizduel.dto;

public final class TopicDtos {
    private TopicDtos() {}

    /** sectionId is only needed when creating a topic. */
    public record TopicRequest(Long sectionId, String name) {}

    public record TopicDto(long id, long sectionId, String sectionName, String name, int questionCount) {}

    /** Adds an existing topic to another section, optionally with copies of its questions. */
    public record CopyTopicRequest(Long sectionId, Boolean copyQuestions) {}
}
