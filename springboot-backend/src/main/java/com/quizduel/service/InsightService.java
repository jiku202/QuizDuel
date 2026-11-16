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
import com.quizduel.exception.ApiException;
import com.quizduel.security.CurrentUser;
import com.quizduel.util.AppClock;
import com.quizduel.util.SectionScope;

/** Read-only numbers: dashboard, analytics, leaderboard and rank tiers. */
@Service
public class InsightService {

    private final JdbcTemplate jdbc;
    private final SectionScope scope;
    private final AppClock clock;
    private final BattleService battles;
    private final RankService ranks;

    public InsightService(JdbcTemplate jdbc, SectionScope scope, AppClock clock, BattleService battles, RankService ranks) {
        this.jdbc = jdbc;
        this.scope = scope;
        this.clock = clock;
        this.battles = battles;
        this.ranks = ranks;
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

    /**
     * Teacher: sectionIds = comma-separated list of their sections; empty = all of their sections.
     * Student: must pass sectionId of a section they joined.
     * One row per student per section, each with the points the student has in that section only.
     */
    public List<LeaderboardEntry> leaderboard(CurrentUser user, String sectionId, String sectionIds) {
        List<Long> ids = new ArrayList<>();
        if (user.isTeacher()) {
            if (sectionIds != null && !sectionIds.isBlank()) {
                for (String part : sectionIds.split(",")) {
                    if (part.isBlank()) continue;
                    ids.addAll(scope.teacherSectionIds(user, part.trim()));
                }
            } else {
                ids = scope.teacherSectionIds(user, sectionId);
            }
        } else {
            if (sectionId == null || sectionId.isBlank()) throw ApiException.badRequest("sectionId is required");
            long sid = SectionScope.parseId(sectionId);
            if (count("SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND student_id = ?", sid, user.id()) == 0) {
                throw ApiException.forbidden("You are not in this section");
            }
            ids = List.of(sid);
        }
        List<RankService.Tier> tiers = ranks.tiers();
        List<LeaderboardEntry> rows = jdbc.query(
                "SELECT e.section_id, s.name AS section_name, u.id, u.name, u.streak, e.points "
                        + "FROM enrollments e JOIN users u ON u.id = e.student_id JOIN sections s ON s.id = e.section_id "
                        + "WHERE e.section_id IN (" + SectionScope.placeholders(ids) + ") "
                        + "ORDER BY e.points DESC, u.name, s.name",
                (rs, i) -> new LeaderboardEntry(0, rs.getLong("id"), rs.getString("name"), rs.getLong("section_id"),
                        rs.getString("section_name"), rs.getInt("points"), rs.getInt("streak"),
                        RankService.rankFor(tiers, rs.getInt("points"))),
                ids.toArray());
        List<LeaderboardEntry> ranked = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            LeaderboardEntry r = rows.get(i);
            ranked.add(new LeaderboardEntry(i + 1, r.studentId(), r.name(), r.sectionId(), r.sectionName(),
                    r.points(), r.streak(), r.rank()));
        }
        return ranked;
    }

    /** The rank tiers (lowest first). Students also get "earned": whether their best section has reached the tier. */
    public List<Map<String, Object>> badges(CurrentUser user) {
        Integer best = user.isStudent()
                ? jdbc.queryForObject("SELECT COALESCE(MAX(points), 0) FROM enrollments WHERE student_id = ?", Integer.class, user.id())
                : null;
        List<Map<String, Object>> out = new ArrayList<>();
        for (RankService.Tier t : ranks.tiers()) {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("id", t.id());
            b.put("name", t.name());
            b.put("description", t.description());
            b.put("pointsThreshold", t.threshold());
            if (best != null) b.put("earned", best >= t.threshold());
            out.add(b);
        }
        return out;
    }
}
