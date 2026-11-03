package com.quizduel.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.quizduel.exception.ApiException;
import com.quizduel.security.CurrentUser;

/** Works out which sections a teacher is asking about: ?sectionId=X (must be theirs) or all of theirs. */
@Component
public class SectionScope {

    private final JdbcTemplate jdbc;

    public SectionScope(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Long> teacherSectionIds(CurrentUser teacher, String sectionIdParam) {
        if (sectionIdParam != null && !sectionIdParam.isBlank()) {
            long sid = parseId(sectionIdParam);
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM sections WHERE id = ? AND teacher_id = ?", Integer.class, sid, teacher.id());
            if (n == null || n == 0) throw ApiException.notFound("Section not found");
            return List.of(sid);
        }
        return jdbc.queryForList("SELECT id FROM sections WHERE teacher_id = ?", Long.class, teacher.id());
    }

    /** "?,?,?" for SQL IN (...). An empty list becomes NULL so the query returns no rows. */
    public static String placeholders(List<Long> ids) {
        return ids.isEmpty() ? "NULL" : ids.stream().map(i -> "?").collect(Collectors.joining(","));
    }

    public static long parseId(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("sectionId must be a number");
        }
    }
}
