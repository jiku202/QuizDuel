package com.quizduel.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quizduel.dto.SectionDtos.AddMembersResponse;
import com.quizduel.dto.SectionDtos.CreateSectionRequest;
import com.quizduel.dto.SectionDtos.JoinResponse;
import com.quizduel.dto.SectionDtos.RosterEntry;
import com.quizduel.dto.SectionDtos.SectionDto;
import com.quizduel.dto.SectionDtos.SectionRow;
import com.quizduel.dto.SectionDtos.UpdateSectionRequest;
import com.quizduel.exception.ApiException;
import com.quizduel.security.CurrentUser;
import com.quizduel.util.Jdbc;
import com.quizduel.util.Validation;

@Service
public class SectionService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no look-alikes (0/O, 1/I)
    private static final Pattern TIME = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private final JdbcTemplate jdbc;
    private final SecureRandom random = new SecureRandom();

    private final RankService ranks;

    public SectionService(JdbcTemplate jdbc, RankService ranks) {
        this.jdbc = jdbc;
        this.ranks = ranks;
    }

    private static final RowMapper<SectionRow> ROW = (rs, i) -> new SectionRow(
            rs.getLong("id"), rs.getLong("teacher_id"), rs.getString("name"), rs.getString("grade_level"),
            rs.getString("subject"), rs.getString("join_code"), rs.getString("battle_time"),
            rs.getBoolean("weekend_battles"));

    private static SectionDto toDto(SectionRow s, Integer studentCount) {
        return new SectionDto(s.id(), s.name(), s.gradeLevel(), s.subject(), s.joinCode(), s.battleTime(),
                s.weekendBattles(), studentCount);
    }

    /** Teacher: sections they own (with student counts). Student: sections they joined. */
    public List<SectionDto> list(CurrentUser user) {
        if (user.isTeacher()) {
            return jdbc.query(
                    "SELECT s.*, (SELECT COUNT(*) FROM enrollments e WHERE e.section_id = s.id) AS student_count "
                            + "FROM sections s WHERE s.teacher_id = ? ORDER BY s.id",
                    (rs, i) -> toDto(ROW.mapRow(rs, i), rs.getInt("student_count")), user.id());
        }
        return jdbc.query(
                "SELECT s.* FROM sections s JOIN enrollments e ON e.section_id = s.id WHERE e.student_id = ? ORDER BY s.id",
                (rs, i) -> toDto(ROW.mapRow(rs, i), null), user.id());
    }

    @Transactional
    public SectionDto create(CurrentUser teacher, CreateSectionRequest req) {
        Validation.required("name", req.name());
        long id = Jdbc.insertReturningId(jdbc,
                "INSERT INTO sections (teacher_id, name, grade_level, subject, join_code) VALUES (?, ?, ?, ?, ?)",
                teacher.id(), req.name().trim(),
                req.gradeLevel() != null ? req.gradeLevel() : "Grade 3",
                req.subject() != null ? req.subject() : "Araling Panlipunan",
                newJoinCode());
        if (req.memberEmails() != null && !req.memberEmails().isEmpty()) {
            enrollByEmail(id, req.memberEmails());
        }
        return toDto(getById(id), studentCount(id));
    }

    @Transactional
    public JoinResponse join(CurrentUser student, String joinCode) {
        Validation.required("joinCode", joinCode);
        List<SectionRow> found = jdbc.query("SELECT * FROM sections WHERE join_code = ?", ROW, joinCode.trim().toUpperCase());
        if (found.isEmpty()) throw ApiException.notFound("No section found for that join code");
        SectionRow section = found.get(0);

        Integer already = jdbc.queryForObject(
                "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ?", Integer.class, student.id(), section.id());
        boolean joined = already == null || already == 0;
        if (joined) jdbc.update("INSERT INTO enrollments (student_id, section_id) VALUES (?, ?)", student.id(), section.id());
        return new JoinResponse(joined, toDto(section, null));
    }

    /** Also used by the Settings page: battleTime ("HH:MM") and weekendBattles. */
    @Transactional
    public SectionDto update(CurrentUser teacher, long sectionId, UpdateSectionRequest b) {
        SectionRow s = getOwned(teacher, sectionId);
        if (b.name() != null && b.name().isBlank()) throw ApiException.badRequest("Section name cannot be empty");
        if (b.battleTime() != null && !TIME.matcher(b.battleTime()).matches()) {
            throw ApiException.badRequest("battleTime must be in 24-hour HH:MM format");
        }
        jdbc.update("UPDATE sections SET name = ?, grade_level = ?, subject = ?, battle_time = ?, weekend_battles = ? WHERE id = ?",
                b.name() != null ? b.name().trim() : s.name(),
                b.gradeLevel() != null ? b.gradeLevel() : s.gradeLevel(),
                b.subject() != null ? b.subject() : s.subject(),
                b.battleTime() != null ? b.battleTime() : s.battleTime(),
                b.weekendBattles() != null ? b.weekendBattles() : s.weekendBattles(),
                s.id());
        return toDto(getById(s.id()), studentCount(s.id()));
    }

    /** Students of one section with the points and rank they have in THIS section. */
    public List<RosterEntry> roster(CurrentUser teacher, long sectionId) {
        SectionRow s = getOwned(teacher, sectionId);
        List<RankService.Tier> tiers = ranks.tiers();
        return jdbc.query(
                "SELECT u.id, u.name, u.email, u.streak, e.points "
                        + "FROM enrollments e JOIN users u ON u.id = e.student_id "
                        + "WHERE e.section_id = ? ORDER BY e.points DESC, u.name",
                (rs, i) -> new RosterEntry(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                        rs.getInt("points"), rs.getInt("streak"), RankService.rankFor(tiers, rs.getInt("points"))),
                s.id());
    }

    /** Teacher deletes a section. Enrollments, questions and battles go with it (ON DELETE CASCADE). */
    @Transactional
    public void delete(CurrentUser teacher, long sectionId) {
        SectionRow s = getOwned(teacher, sectionId);
        jdbc.update("DELETE FROM sections WHERE id = ?", s.id());
    }

    /** Teacher adds registered students to a section by email. */
    @Transactional
    public AddMembersResponse addMembers(CurrentUser teacher, long sectionId, List<String> emails) {
        SectionRow s = getOwned(teacher, sectionId);
        if (emails == null || emails.stream().noneMatch(e -> e != null && !e.isBlank())) {
            throw ApiException.badRequest("Enter at least one student email");
        }
        return enrollByEmail(s.id(), emails);
    }

    /** Teacher removes a student from a section (the student's account is kept). */
    @Transactional
    public void removeMember(CurrentUser teacher, long sectionId, long studentId) {
        SectionRow s = getOwned(teacher, sectionId);
        int removed = jdbc.update("DELETE FROM enrollments WHERE section_id = ? AND student_id = ?", s.id(), studentId);
        if (removed == 0) throw ApiException.notFound("Student is not in this section");
    }

    // ---------- shared helpers (also used by other services) ----------

    public SectionRow getOwned(CurrentUser teacher, long sectionId) {
        List<SectionRow> rows = jdbc.query("SELECT * FROM sections WHERE id = ? AND teacher_id = ?", ROW, sectionId, teacher.id());
        if (rows.isEmpty()) throw ApiException.notFound("Section not found");
        return rows.get(0);
    }

    private int studentCount(long sectionId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM enrollments WHERE section_id = ?", Integer.class, sectionId);
        return n == null ? 0 : n;
    }

    /** Enrolls each email that belongs to a student account; reports the ones it could not add. */
    private AddMembersResponse enrollByEmail(long sectionId, List<String> emails) {
        Set<String> unique = new LinkedHashSet<>();
        for (String e : emails) {
            if (e != null && !e.isBlank()) unique.add(e.trim().toLowerCase());
        }
        List<RankService.Tier> tiers = ranks.tiers();
        List<RosterEntry> added = new ArrayList<>();
        List<String> notFound = new ArrayList<>();
        List<String> alreadyIn = new ArrayList<>();
        for (String email : unique) {
            List<RosterEntry> users = jdbc.query(
                    "SELECT id, name, email, streak FROM users WHERE LOWER(email) = ? AND role = 'student'",
                    (rs, i) -> new RosterEntry(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                            0, rs.getInt("streak"), RankService.rankFor(tiers, 0)),
                    email);
            if (users.isEmpty()) { notFound.add(email); continue; }
            RosterEntry student = users.get(0);
            Integer already = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ?", Integer.class, student.id(), sectionId);
            if (already != null && already > 0) { alreadyIn.add(email); continue; }
            jdbc.update("INSERT INTO enrollments (student_id, section_id) VALUES (?, ?)", student.id(), sectionId);
            added.add(student);
        }
        return new AddMembersResponse(added, notFound, alreadyIn);
    }

    private SectionRow getById(long id) {
        return jdbc.query("SELECT * FROM sections WHERE id = ?", ROW, id).get(0);
    }

    private String newJoinCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < 6; i++) code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            Integer taken = jdbc.queryForObject("SELECT COUNT(*) FROM sections WHERE join_code = ?", Integer.class, code.toString());
            if (taken == null || taken == 0) return code.toString();
        }
        throw new ApiException(500, "Could not generate a unique join code");
    }
}
