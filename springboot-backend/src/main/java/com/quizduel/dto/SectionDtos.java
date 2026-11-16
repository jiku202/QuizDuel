package com.quizduel.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public final class SectionDtos {
    private SectionDtos() {}

    /** memberEmails is optional: registered student emails to enroll right away. */
    public record CreateSectionRequest(String name, String gradeLevel, String subject, List<String> memberEmails) {}

    public record UpdateSectionRequest(String name, String gradeLevel, String subject, String battleTime, Boolean weekendBattles) {}

    public record AddMembersRequest(List<String> emails) {}

    /** added = newly enrolled students; notFound = emails with no student account; alreadyIn = already in the section. */
    public record AddMembersResponse(List<RosterEntry> added, List<String> notFound, List<String> alreadyIn) {}

    public record JoinSectionRequest(String joinCode) {}

    /** studentCount is only filled in for teachers (omitted from the JSON when null). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SectionDto(long id, String name, String gradeLevel, String subject, String joinCode,
                             String battleTime, boolean weekendBattles, Integer studentCount) {}

    public record JoinResponse(boolean joined, SectionDto section) {}

    /** points and rank are for this section only. */
    public record RosterEntry(long id, String name, String email, int points, int streak, String rank) {}

    public record BadgeDto(long id, String name, String description, Integer pointsThreshold) {}

    /** Internal row used by services (not sent to the client). */
    public record SectionRow(long id, long teacherId, String name, String gradeLevel, String subject,
                             String joinCode, String battleTime, boolean weekendBattles) {}
}
