package com.quizduel.service;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quizduel.dto.TopicDtos.CopyTopicRequest;
import com.quizduel.dto.TopicDtos.TopicDto;
import com.quizduel.dto.TopicDtos.TopicRequest;
import com.quizduel.exception.ApiException;
import com.quizduel.security.CurrentUser;
import com.quizduel.util.Jdbc;
import com.quizduel.util.SectionScope;
import com.quizduel.util.Validation;

/** Topics belong to a section; questions live inside a topic (questions.topic holds the topic name). */
@Service
public class TopicService {

    private static final String SELECT =
            "SELECT t.id, t.section_id, s.name AS section_name, t.name, "
                    + "(SELECT COUNT(*) FROM questions q WHERE q.section_id = t.section_id AND q.topic = t.name) AS question_count "
                    + "FROM topics t JOIN sections s ON s.id = t.section_id ";

    private static final RowMapper<TopicDto> ROW = (rs, i) -> new TopicDto(
            rs.getLong("id"), rs.getLong("section_id"), rs.getString("section_name"), rs.getString("name"),
            rs.getInt("question_count"));

    private final JdbcTemplate jdbc;
    private final SectionService sections;
    private final SectionScope scope;

    public TopicService(JdbcTemplate jdbc, SectionService sections, SectionScope scope) {
        this.jdbc = jdbc;
        this.sections = sections;
        this.scope = scope;
    }

    /** Topics of one section, or of all the teacher's sections when sectionId is empty. */
    @Transactional
    public List<TopicDto> list(CurrentUser teacher, String sectionId) {
        List<Long> ids = scope.teacherSectionIds(teacher, sectionId);
        for (Long id : ids) {
            // topics that only exist on older questions are picked up automatically
            List<String> missing = jdbc.queryForList(
                    "SELECT DISTINCT q.topic FROM questions q WHERE q.section_id = ? "
                            + "AND NOT EXISTS (SELECT 1 FROM topics t WHERE t.section_id = q.section_id AND t.name = q.topic)",
                    String.class, id);
            for (String name : missing) ensure(id, name);
        }
        return jdbc.query(SELECT + "WHERE t.section_id IN (" + SectionScope.placeholders(ids) + ") ORDER BY s.name, t.name",
                ROW, ids.toArray());
    }

    @Transactional
    public TopicDto create(CurrentUser teacher, TopicRequest b) {
        Validation.required("sectionId", b.sectionId(), "name", b.name());
        long sectionId = sections.getOwned(teacher, b.sectionId()).id();
        String name = cleanName(b.name());
        if (nameTaken(sectionId, name, 0)) throw ApiException.conflict("This section already has a topic named \"" + name + "\"");
        long id = Jdbc.insertReturningId(jdbc, "INSERT INTO topics (section_id, name) VALUES (?, ?)", sectionId, name);
        return jdbc.query(SELECT + "WHERE t.id = ?", ROW, id).get(0);
    }

    /** Renames the topic and every question inside it. */
    @Transactional
    public TopicDto rename(CurrentUser teacher, long id, TopicRequest b) {
        TopicDto t = owned(teacher, id);
        Validation.required("name", b.name());
        String name = cleanName(b.name());
        if (nameTaken(t.sectionId(), name, t.id())) throw ApiException.conflict("This section already has a topic named \"" + name + "\"");
        jdbc.update("UPDATE topics SET name = ? WHERE id = ?", name, t.id());
        jdbc.update("UPDATE questions SET topic = ? WHERE section_id = ? AND topic = ?", name, t.sectionId(), t.name());
        return jdbc.query(SELECT + "WHERE t.id = ?", ROW, t.id()).get(0);
    }

    /** Adds an existing topic to another of the teacher's sections (optionally with copies of its questions). */
    @Transactional
    public TopicDto copyTo(CurrentUser teacher, long id, CopyTopicRequest b) {
        TopicDto src = owned(teacher, id);
        Validation.required("sectionId", b.sectionId());
        long target = sections.getOwned(teacher, b.sectionId()).id();
        if (target == src.sectionId()) throw ApiException.badRequest("Pick a different section");
        if (nameTaken(target, src.name(), 0)) {
            throw ApiException.conflict("That section already has a topic named \"" + src.name() + "\"");
        }
        long newId = Jdbc.insertReturningId(jdbc, "INSERT INTO topics (section_id, name) VALUES (?, ?)", target, src.name());
        if (Boolean.TRUE.equals(b.copyQuestions())) {
            jdbc.update("INSERT INTO questions (section_id, topic, text, choice_a, choice_b, choice_c, choice_d, correct_answer, difficulty) "
                    + "SELECT CAST(? AS BIGINT), topic, text, choice_a, choice_b, choice_c, choice_d, correct_answer, difficulty "
                    + "FROM questions WHERE section_id = ? AND topic = ?", target, src.sectionId(), src.name());
        }
        return jdbc.query(SELECT + "WHERE t.id = ?", ROW, newId).get(0);
    }

    /** Deletes the topic together with all of its questions. */
    @Transactional
    public void delete(CurrentUser teacher, long id) {
        TopicDto t = owned(teacher, id);
        jdbc.update("DELETE FROM questions WHERE section_id = ? AND topic = ?", t.sectionId(), t.name());
        jdbc.update("DELETE FROM topics WHERE id = ?", t.id());
    }

    /** Makes sure a topic row exists (used when a question is saved). */
    public void ensure(long sectionId, String name) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM topics WHERE section_id = ? AND name = ?", Integer.class, sectionId, name);
        if (n == null || n == 0) {
            try {
                jdbc.update("INSERT INTO topics (section_id, name) VALUES (?, ?)", sectionId, name);
            } catch (DuplicateKeyException ignored) {
                // another request added it a moment ago
            }
        }
    }

    private TopicDto owned(CurrentUser teacher, long id) {
        List<TopicDto> rows = jdbc.query(
                SELECT + "WHERE t.id = ? AND s.teacher_id = ?", ROW, id, teacher.id());
        if (rows.isEmpty()) throw ApiException.notFound("Topic not found");
        return rows.get(0);
    }

    private boolean nameTaken(long sectionId, String name, long exceptId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM topics WHERE section_id = ? AND LOWER(name) = LOWER(?) AND id <> ?",
                Integer.class, sectionId, name, exceptId);
        return n != null && n > 0;
    }

    private static String cleanName(String raw) {
        String name = raw.trim();
        if (name.length() > 120) throw ApiException.badRequest("Topic name must be 120 characters or fewer");
        return name;
    }
}
