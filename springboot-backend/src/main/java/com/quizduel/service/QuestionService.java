package com.quizduel.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quizduel.dto.QuestionDtos.QuestionDto;
import com.quizduel.dto.QuestionDtos.QuestionRequest;
import com.quizduel.exception.ApiException;
import com.quizduel.security.CurrentUser;
import com.quizduel.util.Jdbc;
import com.quizduel.util.SectionScope;
import com.quizduel.util.Validation;

@Service
public class QuestionService {

    private static final List<String> LETTERS = List.of("A", "B", "C", "D");
    private static final List<String> DIFFICULTIES = List.of("Easy", "Medium", "Hard");

    private final JdbcTemplate jdbc;
    private final SectionScope scope;
    private final SectionService sections;

    public QuestionService(JdbcTemplate jdbc, SectionScope scope, SectionService sections) {
        this.jdbc = jdbc;
        this.scope = scope;
        this.sections = sections;
    }

    private static final RowMapper<QuestionDto> ROW = (rs, i) -> {
        Map<String, String> choices = new LinkedHashMap<>();
        choices.put("A", rs.getString("choice_a"));
        choices.put("B", rs.getString("choice_b"));
        choices.put("C", rs.getString("choice_c"));
        choices.put("D", rs.getString("choice_d"));
        return new QuestionDto(rs.getLong("id"), rs.getLong("section_id"), rs.getString("topic"), rs.getString("text"),
                choices, rs.getString("correct_answer"), rs.getString("difficulty"));
    };

    public List<QuestionDto> list(CurrentUser teacher, String sectionId, String topic) {
        List<Long> ids = scope.teacherSectionIds(teacher, sectionId);
        List<Object> params = new ArrayList<>(ids);
        String sql = "SELECT * FROM questions WHERE section_id IN (" + SectionScope.placeholders(ids) + ")";
        if (topic != null && !topic.isBlank()) {
            sql += " AND topic = ?";
            params.add(topic);
        }
        return jdbc.query(sql + " ORDER BY id", ROW, params.toArray());
    }

    public List<String> topics(CurrentUser teacher, String sectionId) {
        List<Long> ids = scope.teacherSectionIds(teacher, sectionId);
        return jdbc.queryForList(
                "SELECT DISTINCT topic FROM questions WHERE section_id IN (" + SectionScope.placeholders(ids) + ") ORDER BY topic",
                String.class, ids.toArray());
    }

    @Transactional
    public QuestionDto create(CurrentUser teacher, QuestionRequest b) {
        Validation.required("sectionId", b.sectionId());
        validate(b, false);
        long sectionId = sections.getOwned(teacher, b.sectionId()).id();
        long id = Jdbc.insertReturningId(jdbc,
                "INSERT INTO questions (section_id, topic, text, choice_a, choice_b, choice_c, choice_d, correct_answer, difficulty) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                sectionId, b.topic(), b.text(), b.choices().get("A"), b.choices().get("B"), b.choices().get("C"),
                b.choices().get("D"), b.correctAnswer(), b.difficulty() != null ? b.difficulty() : "Easy");
        return jdbc.query("SELECT * FROM questions WHERE id = ?", ROW, id).get(0);
    }

    /** Any subset of the create fields. */
    @Transactional
    public QuestionDto update(CurrentUser teacher, long id, QuestionRequest b) {
        QuestionDto q = owned(teacher, id);
        validate(b, true);
        Map<String, String> c = b.choices();
        jdbc.update("UPDATE questions SET topic = ?, text = ?, choice_a = ?, choice_b = ?, choice_c = ?, choice_d = ?, "
                        + "correct_answer = ?, difficulty = ? WHERE id = ?",
                b.topic() != null ? b.topic() : q.topic(),
                b.text() != null ? b.text() : q.text(),
                c != null && c.get("A") != null ? c.get("A") : q.choices().get("A"),
                c != null && c.get("B") != null ? c.get("B") : q.choices().get("B"),
                c != null && c.get("C") != null ? c.get("C") : q.choices().get("C"),
                c != null && c.get("D") != null ? c.get("D") : q.choices().get("D"),
                b.correctAnswer() != null ? b.correctAnswer() : q.correctAnswer(),
                b.difficulty() != null ? b.difficulty() : q.difficulty(),
                id);
        return jdbc.query("SELECT * FROM questions WHERE id = ?", ROW, id).get(0);
    }

    @Transactional
    public void delete(CurrentUser teacher, long id) {
        owned(teacher, id);
        jdbc.update("DELETE FROM questions WHERE id = ?", id);
    }

    private QuestionDto owned(CurrentUser teacher, long id) {
        List<QuestionDto> rows = jdbc.query(
                "SELECT q.* FROM questions q JOIN sections s ON s.id = q.section_id WHERE q.id = ? AND s.teacher_id = ?",
                ROW, id, teacher.id());
        if (rows.isEmpty()) throw ApiException.notFound("Question not found");
        return rows.get(0);
    }

    private void validate(QuestionRequest b, boolean partial) {
        if (!partial) Validation.required("topic", b.topic(), "text", b.text(), "choices", b.choices(), "correctAnswer", b.correctAnswer());
        if (b.choices() != null) {
            for (String letter : LETTERS) {
                String v = b.choices().get(letter);
                if (v == null || v.isBlank()) {
                    throw ApiException.badRequest("choices must include non-empty A, B, C and D");
                }
            }
        }
        if (b.correctAnswer() != null && !LETTERS.contains(b.correctAnswer())) {
            throw ApiException.badRequest("correctAnswer must be A, B, C or D");
        }
        if (b.difficulty() != null && !DIFFICULTIES.contains(b.difficulty())) {
            throw ApiException.badRequest("difficulty must be one of " + String.join(", ", DIFFICULTIES));
        }
    }
}
