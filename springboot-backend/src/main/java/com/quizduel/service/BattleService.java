package com.quizduel.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quizduel.config.GameRules;
import com.quizduel.dto.BattleDtos.AnswerItem;
import com.quizduel.dto.BattleDtos.BattleRow;
import com.quizduel.dto.BattleDtos.GenerateResult;
import com.quizduel.dto.BattleDtos.SubmitResponse;
import com.quizduel.dto.SectionDtos.SectionRow;
import com.quizduel.exception.ApiException;
import com.quizduel.security.CurrentUser;
import com.quizduel.util.AppClock;
import com.quizduel.util.Jdbc;
import com.quizduel.util.SectionScope;
import com.quizduel.util.Validation;

/** Daily matchmaking, answer scoring, winners, points, streaks and badges. */
@Service
public class BattleService {

    private static final Map<String, String> STATUS_LABEL =
            Map.of("pending", "Pending", "in_progress", "In Progress", "completed", "Completed");

    /** Shared SELECT behind every battle table. player2 NULL means the Practice Bot. */
    private static final String BATTLE_LIST_SQL =
            "SELECT b.id, b.section_id, b.battle_date, b.topic, b.status, b.result, b.player1_score, b.player2_score, "
                    + "u1.name AS p1_name, COALESCE(u2.name, 'Practice Bot') AS p2_name "
                    + "FROM battles b JOIN users u1 ON u1.id = b.player1_id LEFT JOIN users u2 ON u2.id = b.player2_id";

    private final JdbcTemplate jdbc;
    private final SectionService sections;
    private final SectionScope scope;
    private final AppClock clock;
    private final Random random = new Random();

    public BattleService(JdbcTemplate jdbc, SectionService sections, SectionScope scope, AppClock clock) {
        this.jdbc = jdbc;
        this.sections = sections;
        this.scope = scope;
        this.clock = clock;
    }

    private static BattleRow mapRow(ResultSet rs, int i) throws SQLException {
        String status = rs.getString("status");
        String result = rs.getString("result");
        String p1 = rs.getString("p1_name");
        String p2 = rs.getString("p2_name");
        String winner = "p1".equals(result) ? p1 : "p2".equals(result) ? p2 : "draw".equals(result) ? "Draw" : null;
        return new BattleRow(rs.getLong("id"), rs.getLong("section_id"), rs.getString("battle_date"),
                rs.getString("topic"), status, STATUS_LABEL.get(status), result,
                rs.getInt("player1_score"), rs.getInt("player2_score"), p1, p2, winner);
    }

    // ------------------------------------------------------------------ teacher queries

    public List<BattleRow> today(CurrentUser teacher, String sectionId) {
        List<Long> ids = scope.teacherSectionIds(teacher, sectionId);
        List<Object> params = new ArrayList<>();
        params.add(clock.today());
        params.addAll(ids);
        return jdbc.query(BATTLE_LIST_SQL + " WHERE b.battle_date = ? AND b.section_id IN (" + SectionScope.placeholders(ids)
                + ") ORDER BY b.id", BattleService::mapRow, params.toArray());
    }

    /** Completed battles, newest first. */
    public List<BattleRow> log(CurrentUser teacher, String sectionId, Integer limit) {
        List<Long> ids = scope.teacherSectionIds(teacher, sectionId);
        int cap = Math.min(limit != null && limit > 0 ? limit : 50, 200);
        List<Object> params = new ArrayList<>(ids);
        params.add(cap);
        return jdbc.query(BATTLE_LIST_SQL + " WHERE b.status = 'completed' AND b.section_id IN (" + SectionScope.placeholders(ids)
                + ") ORDER BY b.battle_date DESC, b.id DESC LIMIT ?", BattleService::mapRow, params.toArray());
    }

    public List<BattleRow> myToday(CurrentUser student) {
        return jdbc.query(BATTLE_LIST_SQL + " WHERE b.battle_date = ? AND (b.player1_id = ? OR b.player2_id = ?) ORDER BY b.id",
                BattleService::mapRow, clock.today(), student.id(), student.id());
    }

    // ------------------------------------------------------------------ matchmaking

    /**
     * Daily random matchmaking for one section. Pairs enrolled students at random (an odd one out plays
     * the Practice Bot), picks one topic of the day and draws up to 10 questions from it per battle.
     * Running it twice on the same day creates nothing the second time.
     */
    @Transactional
    public GenerateResult generate(CurrentUser teacher, Long sectionId) {
        Validation.required("sectionId", sectionId);
        SectionRow section = sections.getOwned(teacher, sectionId);
        String date = clock.today();

        Integer existing = jdbc.queryForObject(
                "SELECT COUNT(*) FROM battles WHERE section_id = ? AND battle_date = ?", Integer.class, section.id(), date);
        if (existing != null && existing > 0) return new GenerateResult(0, true, date, null);

        DayOfWeek day = LocalDate.parse(date).getDayOfWeek();
        if ((day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) && !section.weekendBattles()) {
            throw ApiException.badRequest("Weekend battles are disabled for this section");
        }

        List<Long> students = jdbc.queryForList("SELECT student_id FROM enrollments WHERE section_id = ?", Long.class, section.id());
        if (students.isEmpty()) throw ApiException.badRequest("No students are enrolled in this section yet");

        List<String> topics = jdbc.queryForList("SELECT DISTINCT topic FROM questions WHERE section_id = ?", String.class, section.id());
        if (topics.isEmpty()) throw ApiException.badRequest("Add questions to the question bank first");

        String topic = topics.get(random.nextInt(topics.size()));
        List<Long> pool = jdbc.queryForList("SELECT id FROM questions WHERE section_id = ? AND topic = ?", Long.class, section.id(), topic);

        List<Long> order = new ArrayList<>(students);
        Collections.shuffle(order, random);

        int created = 0;
        for (int i = 0; i < order.size(); i += 2) {
            Long p1 = order.get(i);
            Long p2 = i + 1 < order.size() ? order.get(i + 1) : null; // null = Practice Bot
            long battleId = Jdbc.insertReturningId(jdbc,
                    "INSERT INTO battles (section_id, player1_id, player2_id, battle_date, topic, status) VALUES (?, ?, ?, ?, ?, 'pending')",
                    section.id(), p1, p2, date, topic);

            List<Long> picks = new ArrayList<>(pool);
            Collections.shuffle(picks, random);
            for (int k = 0; k < Math.min(GameRules.QUESTIONS_PER_BATTLE, picks.size()); k++) {
                jdbc.update("INSERT INTO battle_questions (battle_id, question_id, seq) VALUES (?, ?, ?)", battleId, picks.get(k), k + 1);
            }
            created++;
        }
        return new GenerateResult(created, false, date, topic);
    }

    // ------------------------------------------------------------------ battle detail

    private record Scores(long p1, Long p2, int s1, int s2) {}

    private record BattleRaw(long id, long sectionId, long player1Id, Long player2Id, String status) {}

    private BattleRaw loadRaw(long battleId) {
        List<BattleRaw> rows = jdbc.query("SELECT id, section_id, player1_id, player2_id, status FROM battles WHERE id = ?",
                (rs, i) -> {
                    long p1 = rs.getLong("player1_id");
                    long second = rs.getLong("player2_id");
                    Long p2 = rs.wasNull() ? null : second;   // must be checked right after reading the column
                    return new BattleRaw(rs.getLong("id"), rs.getLong("section_id"), p1, p2, rs.getString("status"));
                }, battleId);
        if (rows.isEmpty()) throw ApiException.notFound("Battle not found");
        return rows.get(0);
    }

    public BattleRow rowById(long battleId) {
        return jdbc.query(BATTLE_LIST_SQL + " WHERE b.id = ?", BattleService::mapRow, battleId).get(0);
    }

    /** Battle + its questions. Correct answers are hidden from students until the battle is completed. */
    public Map<String, Object> detail(CurrentUser user, long battleId) {
        BattleRaw b = loadRaw(battleId);
        boolean isTeacher = false;
        if (user.isTeacher()) {
            Integer owns = jdbc.queryForObject("SELECT COUNT(*) FROM sections WHERE id = ? AND teacher_id = ?",
                    Integer.class, b.sectionId(), user.id());
            isTeacher = owns != null && owns > 0;
        }
        boolean isPlayer = user.id() == b.player1Id() || (b.player2Id() != null && user.id() == b.player2Id());
        if (!isTeacher && !isPlayer) throw ApiException.forbidden("You can't view this battle");

        boolean showAnswers = isTeacher || "completed".equals(b.status());
        List<Map<String, Object>> questions = jdbc.query(
                "SELECT q.id, q.text, q.choice_a, q.choice_b, q.choice_c, q.choice_d, q.correct_answer "
                        + "FROM battle_questions bq JOIN questions q ON q.id = bq.question_id WHERE bq.battle_id = ? ORDER BY bq.seq",
                (rs, i) -> {
                    Map<String, Object> q = new LinkedHashMap<>();
                    q.put("id", rs.getLong("id"));
                    q.put("text", rs.getString("text"));
                    Map<String, String> choices = new LinkedHashMap<>();
                    choices.put("A", rs.getString("choice_a"));
                    choices.put("B", rs.getString("choice_b"));
                    choices.put("C", rs.getString("choice_c"));
                    choices.put("D", rs.getString("choice_d"));
                    q.put("choices", choices);
                    if (showAnswers) q.put("correctAnswer", rs.getString("correct_answer"));
                    return q;
                }, battleId);

        BattleRow r = rowById(battleId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", r.id());
        out.put("sectionId", r.sectionId());
        out.put("date", r.date());
        out.put("topic", r.topic());
        out.put("status", r.status());
        out.put("statusLabel", r.statusLabel());
        out.put("result", r.result());
        out.put("player1Score", r.player1Score());
        out.put("player2Score", r.player2Score());
        out.put("player1", r.player1());
        out.put("player2", r.player2());
        out.put("winner", r.winner());
        out.put("questions", questions);
        return out;
    }

    // ------------------------------------------------------------------ playing

    /**
     * Records one student's answers and scores them. When the opponent has also finished (or the opponent
     * is the Practice Bot) the battle is finalized: winner, points, streaks and badges.
     */
    @Transactional
    public SubmitResponse submitAnswers(CurrentUser student, long battleId, List<AnswerItem> answers) {
        BattleRaw b = loadRaw(battleId);
        boolean isP1 = b.player1Id() == student.id();
        boolean isP2 = b.player2Id() != null && b.player2Id() == student.id();
        if (!isP1 && !isP2) throw ApiException.forbidden("You are not a player in this battle");
        if ("completed".equals(b.status())) throw ApiException.badRequest("This battle is already completed");

        Integer already = jdbc.queryForObject("SELECT COUNT(*) FROM battle_answers WHERE battle_id = ? AND student_id = ?",
                Integer.class, battleId, student.id());
        if (already != null && already > 0) throw ApiException.badRequest("You already submitted your answers for this battle");
        if (answers == null || answers.isEmpty()) throw ApiException.badRequest("answers must be a non-empty array");

        Set<Long> allowed = new HashSet<>(jdbc.queryForList("SELECT question_id FROM battle_questions WHERE battle_id = ?", Long.class, battleId));
        Set<Long> seen = new HashSet<>();
        int score = 0;
        for (AnswerItem a : answers) {
            Long qid = a.questionId();
            if (qid == null || !allowed.contains(qid) || !seen.add(qid)) {
                throw ApiException.badRequest("Question " + qid + " does not belong to this battle (or was answered twice)");
            }
            String selected = a.selected() != null ? a.selected().toUpperCase() : null;
            String correctAnswer = jdbc.queryForObject("SELECT correct_answer FROM questions WHERE id = ?", String.class, qid);
            int correct = selected != null && selected.equals(correctAnswer) ? 1 : 0;
            score += correct;
            double time = a.timeTaken() != null ? Math.max(0, a.timeTaken()) : 0;
            jdbc.update("INSERT INTO battle_answers (battle_id, student_id, question_id, selected_answer, is_correct, time_taken) "
                    + "VALUES (?, ?, ?, ?, ?, ?)", battleId, student.id(), qid, selected, correct, time);
        }

        jdbc.update("UPDATE battles SET " + (isP1 ? "player1_score" : "player2_score") + " = ?, status = 'in_progress' WHERE id = ?",
                score, battleId);

        if (b.player2Id() == null) {
            // Practice Bot answers 50-90% of the questions correctly
            int botScore = (int) Math.round(allowed.size() * (0.5 + random.nextDouble() * 0.4));
            jdbc.update("UPDATE battles SET player2_score = ? WHERE id = ?", botScore, battleId);
        }

        Long opponentId = isP1 ? b.player2Id() : (Long) b.player1Id();
        boolean opponentDone = opponentId == null;
        if (!opponentDone) {
            Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM battle_answers WHERE battle_id = ? AND student_id = ?",
                    Integer.class, battleId, opponentId);
            opponentDone = n != null && n > 0;
        }
        if (opponentDone) finalizeBattle(battleId);

        return new SubmitResponse(score, allowed.size(), rowById(battleId));
    }

    /**
     * Higher score wins. A tie between two students is broken by faster total time, otherwise it is a draw.
     * The winner gets points, a streak +1 and any newly earned badges; the loser's streak resets.
     */
    private void finalizeBattle(long battleId) {
        Scores sc = jdbc.query(
                "SELECT player1_id, player2_id, player1_score, player2_score FROM battles WHERE id = ?",
                (rs, i) -> {
                    long second = rs.getLong("player2_id");
                    Long p2id = rs.wasNull() ? null : second;
                    return new Scores(rs.getLong("player1_id"), p2id, rs.getInt("player1_score"), rs.getInt("player2_score"));
                }, battleId).get(0);
        long p1 = sc.p1();
        Long p2 = sc.p2();
        int s1 = sc.s1();
        int s2 = sc.s2();

        String result;
        if (s1 > s2) result = "p1";
        else if (s2 > s1) result = "p2";
        else if (p2 != null) {
            double t1 = totalTime(battleId, p1);
            double t2 = totalTime(battleId, p2);
            result = t1 < t2 ? "p1" : t2 < t1 ? "p2" : "draw";
        } else result = "draw";

        Long winnerId = "p1".equals(result) ? (Long) p1 : "p2".equals(result) ? p2 : null;
        jdbc.update("UPDATE battles SET result = ?, winner_id = ?, status = 'completed' WHERE id = ?", result, winnerId, battleId);

        if (winnerId != null) {
            int points = p2 == null ? GameRules.BOT_WIN_POINTS : GameRules.WIN_POINTS;
            jdbc.update("UPDATE users SET total_points = total_points + ?, streak = streak + 1 WHERE id = ?", points, winnerId);
            awardEarnedBadges(winnerId);
        }
        if (!"draw".equals(result)) {
            for (Long id : new Long[] {p1, p2}) {
                if (id != null && !id.equals(winnerId)) jdbc.update("UPDATE users SET streak = 0 WHERE id = ?", id);
            }
        }
    }

    private double totalTime(long battleId, long studentId) {
        Double t = jdbc.queryForObject("SELECT COALESCE(SUM(time_taken), 0) FROM battle_answers WHERE battle_id = ? AND student_id = ?",
                Double.class, battleId, studentId);
        return t == null ? 0 : t;
    }

    /** Hands out every point-threshold badge the user now qualifies for. */
    public void awardEarnedBadges(long userId) {
        Integer points = jdbc.queryForObject("SELECT total_points FROM users WHERE id = ?", Integer.class, userId);
        jdbc.update("INSERT INTO user_badges (user_id, badge_id) "
                + "SELECT ?, b.id FROM badges b WHERE b.points_threshold IS NOT NULL AND b.points_threshold <= ? "
                + "AND NOT EXISTS (SELECT 1 FROM user_badges ub WHERE ub.user_id = ? AND ub.badge_id = b.id)",
                userId, points == null ? 0 : points, userId);
    }
}
