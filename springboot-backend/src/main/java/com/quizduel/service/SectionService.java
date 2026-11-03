package com.quizduel.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quizduel.dto.SectionDtos.AwardResponse;
import com.quizduel.dto.SectionDtos.BadgeDto;
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

    public SectionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
        return toDto(getById(id), null);
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
        if (b.battleTime() != null && !TIME.matcher(b.battleTime()).matches()) {
            throw ApiException.badRequest("battleTime must be in 24-hour HH:MM format");
        }
        jdbc.update("UPDATE sections SET name = ?, grade_level = ?, subject = ?, battle_time = ?, weekend_battles = ? WHERE id = ?",
                b.name() != null ? b.name() : s.name(),
                b.gradeLevel() != null ? b.gradeLevel() : s.gradeLevel(),
                b.subject() != null ? b.subject() : s.subject(),
                b.battleTime() != null ? b.battleTime() : s.battleTime(),
                b.weekendBattles() != null ? b.weekendBattles() : s.weekendBattles(),
                s.id());
        return toDto(getById(s.id()), null);
    }

    public List<RosterEntry> roster(CurrentUser teacher, long sectionId) {
        SectionRow s = getOwned(teacher, sectionId);
        return jdbc.query(
                "SELECT u.id, u.name, u.email, u.total_points, u.streak, "
                        + "(SELECT COUNT(*) FROM user_badges ub WHERE ub.user_id = u.id) AS badges "
                        + "FROM enrollments e JOIN users u ON u.id = e.student_id "
                        + "WHERE e.section_id = ? ORDER BY u.total_points DESC, u.name",
                (rs, i) -> new RosterEntry(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                        rs.getInt("total_points"), rs.getInt("streak"), rs.getInt("badges")),
                s.id());
    }

    /** Teacher hands a badge to a student in their section. */
    @Transactional
    public AwardResponse awardBadge(CurrentUser teacher, long sectionId, long studentId, Long badgeId) {
        SectionRow s = getOwned(teacher, sectionId);
        Validation.required("badgeId", badgeId);
        Integer enrolled = jdbc.queryForObject(
                "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND student_id = ?", Integer.class, s.id(), studentId);
        if (enrolled == null || enrolled == 0) throw ApiException.notFound("Student is not in this section");

        List<BadgeDto> badges = jdbc.query("SELECT id, name, description, points_threshold FROM badges WHERE id = ?",
                (rs, i) -> {
                    int t = rs.getInt("points_threshold");
                    Integer threshold = rs.wasNull() ? null : t;   // must be checked right after reading the column
                    return new BadgeDto(rs.getLong("id"), rs.getString("name"), rs.getString("description"), threshold);
                }, badgeId);
        if (badges.isEmpty()) throw ApiException.notFound("Badge not found");

        Integer has = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_badges WHERE user_id = ? AND badge_id = ?", Integer.class, studentId, badgeId);
        boolean awarded = has == null || has == 0;
        if (awarded) jdbc.update("INSERT INTO user_badges (user_id, badge_id) VALUES (?, ?)", studentId, badgeId);
        return new AwardResponse(awarded, badges.get(0));
    }

    // ---------- shared helpers (also used by other services) ----------

    public SectionRow getOwned(CurrentUser teacher, long sectionId) {
        List<SectionRow> rows = jdbc.query("SELECT * FROM sections WHERE id = ? AND teacher_id = ?", ROW, sectionId, teacher.id());
        if (rows.isEmpty()) throw ApiException.notFound("Section not found");
        return rows.get(0);
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
