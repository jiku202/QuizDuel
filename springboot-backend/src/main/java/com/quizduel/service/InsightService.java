package com.quizduel.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.quizduel.dto.BattleDtos.BattleRow;
import com.quizduel.dto.InsightDtos.Analytics;
import com.quizduel.dto.InsightDtos.Dashboard;
import com.quizduel.dto.InsightDtos.LeaderboardEntry;
import com.quizduel.dto.InsightDtos.Stats;
import com.quizduel.dto.InsightDtos.TopicMastery;
import com.quizduel.dto.SectionDtos.BadgeDto;
import com.quizduel.exception.ApiException;
import com.quizduel.security.CurrentUser;
import com.quizduel.util.AppClock;
import com.quizduel.util.Jdbc;
import com.quizduel.util.SectionScope;
import com.quizduel.util.Validation;

/** Read-only numbers: dashboard, analytics, leaderboard, badges. */
@Service
public class InsightService {

    private final JdbcTemplate jdbc;
    private final SectionScope scope;
    private final AppClock clock;
    private final BattleService battles;

    public InsightService(JdbcTemplate jdbc, SectionScope scope, AppClock clock, BattleService battles) {
        this.jdbc = jdbc;
        this.scope = scope;
        this.clock = clock;
        this.battles = battles;
    }

    private int count(String sql, Object... args) {
        Integer n = jdbc.queryForObject(sql, Integer.class, args);
        return n == null ? 0 : n;
    }

    private List<Object> withIds(List<Long> ids, Object... before) {
        List<Object> params = new ArrayList<>();
        for (Object b : before) params.add(b);
        params.addAll(ids);
        return params;
    }

    /** Percentage of all answers that were correct, 0 when nothing has been played yet. */
    private int averageScore(List<Long> ids) {
        return jdbc.query(
                "SELECT COUNT(*) AS n, COALESCE(SUM(a.is_correct), 0) AS correct FROM battle_answers a "
                        + "JOIN battles b ON b.id = a.battle_id WHERE b.section_id IN (" + SectionScope.placeholders(ids) + ")",
                (rs, i) -> rs.getInt("n") == 0 ? 0 : (int) Math.round(rs.getInt("correct") * 100.0 / rs.getInt("n")),
                ids.toArray()).get(0);
    }

    private List<TopicMastery> topicMastery(List<Long> ids) {
        return jdbc.query(
                "SELECT q.topic AS topic, COUNT(*) AS n, COALESCE(SUM(a.is_correct), 0) AS correct FROM battle_answers a "
                        + "JOIN questions q ON q.id = a.question_id JOIN battles b ON b.id = a.battle_id "
                        + "WHERE b.section_id IN (" + SectionScope.placeholders(ids) + ") GROUP BY q.topic ORDER BY q.topic",
                (rs, i) -> new TopicMastery(rs.getString("topic"), (int) Math.round(rs.getInt("correct") * 100.0 / rs.getInt("n"))),
                ids.toArray());
    }

    private int students(List<Long> ids) {
        return count("SELECT COUNT(DISTINCT student_id) FROM enrollments WHERE section_id IN (" + SectionScope.placeholders(ids) + ")", ids.toArray());
    }

    public Dashboard dashboard(CurrentUser teacher, String sectionId) {
        List<Long> ids = scope.teacherSectionIds(teacher, sectionId);
        int active = count("SELECT COUNT(*) FROM battles WHERE battle_date = ? AND status <> 'completed' AND section_id IN ("
                + SectionScope.placeholders(ids) + ")", withIds(ids, clock.today()).toArray());
        List<BattleRow> today = battles.today(teacher, sectionId);
        return new Dashboard(new Stats(active, students(ids), averageScore(ids), ids.size()), topicMastery(ids), today);
    }

    public Analytics analytics(CurrentUser teacher, String sectionId) {
        List<Long> ids = scope.teacherSectionIds(teacher, sectionId);
        int battlesToday = count("SELECT COUNT(*) FROM battles WHERE battle_date = ? AND section_id IN ("
                + SectionScope.placeholders(ids) + ")", withIds(ids, clock.today()).toArray());
        return new Analytics(averageScore(ids), battlesToday, students(ids), topicMastery(ids));
    }

    /** Teacher: their sections (or ?sectionId). Student: must pass a section they joined. */
    public List<LeaderboardEntry> leaderboard(CurrentUser user, String sectionId) {
        List<Long> ids;
        if (user.isTeacher()) {
            ids = scope.teacherSectionIds(user, sectionId);
        } else {
            if (sectionId == null || sectionId.isBlank()) throw ApiException.badRequest("sectionId is required");
            long sid = SectionScope.parseId(sectionId);
            if (count("SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND student_id = ?", sid, user.id()) == 0) {
                throw ApiException.forbidden("You are not in this section");
            }
            ids = List.of(sid);
        }
        List<LeaderboardEntry> rows = jdbc.query(
                "SELECT u.id, u.name, u.total_points, u.streak, (SELECT COUNT(*) FROM user_badges ub WHERE ub.user_id = u.id) AS badges "
                        + "FROM users u WHERE u.id IN (SELECT student_id FROM enrollments WHERE section_id IN ("
                        + SectionScope.placeholders(ids) + ")) ORDER BY u.total_points DESC, u.name",
                (rs, i) -> new LeaderboardEntry(0, rs.getLong("id"), rs.getString("name"), rs.getInt("total_points"),
                        rs.getInt("streak"), rs.getInt("badges")),
                ids.toArray());
        List<LeaderboardEntry> ranked = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            LeaderboardEntry r = rows.get(i);
            ranked.add(new LeaderboardEntry(i + 1, r.studentId(), r.name(), r.points(), r.streak(), r.badges()));
        }
        return ranked;
    }

    /** All badges. Students also get "earned" (true/false); teachers don't. */
    public List<Map<String, Object>> badges(CurrentUser user) {
        List<Long> earned = user.isStudent()
                ? jdbc.queryForList("SELECT badge_id FROM user_badges WHERE user_id = ?", Long.class, user.id())
                : List.of();
        return jdbc.query("SELECT id, name, description, points_threshold FROM badges ORDER BY id", (rs, i) -> {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("id", rs.getLong("id"));
            b.put("name", rs.getString("name"));
            b.put("description", rs.getString("description"));
            int t = rs.getInt("points_threshold");
            b.put("pointsThreshold", rs.wasNull() ? null : t);
            if (user.isStudent()) b.put("earned", earned.contains(rs.getLong("id")));
            return b;
        });
    }

    /** Teacher creates a custom badge (no point threshold) for manual awards. */
    public BadgeDto createBadge(String name, String description) {
        Validation.required("name", name);
        if (count("SELECT COUNT(*) FROM badges WHERE name = ?", name) > 0) {
            throw ApiException.conflict("A badge with that name already exists");
        }
        long id = Jdbc.insertReturningId(jdbc, "INSERT INTO badges (name, description, points_threshold) VALUES (?, ?, NULL)", name, description);
        return new BadgeDto(id, name, description, null);
    }
}
