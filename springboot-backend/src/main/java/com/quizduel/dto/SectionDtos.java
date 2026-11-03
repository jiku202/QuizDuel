package com.quizduel.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public final class SectionDtos {
    private SectionDtos() {}

    public record CreateSectionRequest(String name, String gradeLevel, String subject) {}

    public record UpdateSectionRequest(String name, String gradeLevel, String subject, String battleTime, Boolean weekendBattles) {}

    public record JoinSectionRequest(String joinCode) {}

    public record AwardBadgeRequest(Long badgeId) {}

    /** studentCount is only filled in for teachers (omitted from the JSON when null). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SectionDto(long id, String name, String gradeLevel, String subject, String joinCode,
                             String battleTime, boolean weekendBattles, Integer studentCount) {}

    public record JoinResponse(boolean joined, SectionDto section) {}

    public record RosterEntry(long id, String name, String email, int points, int streak, int badges) {}

    public record BadgeDto(long id, String name, String description, Integer pointsThreshold) {}

    public record AwardResponse(boolean awarded, BadgeDto badge) {}

    /** Internal row used by services (not sent to the client). */
    public record SectionRow(long id, long teacherId, String name, String gradeLevel, String subject,
                             String joinCode, String battleTime, boolean weekendBattles) {}
}
