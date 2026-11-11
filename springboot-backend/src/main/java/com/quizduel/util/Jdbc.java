package com.quizduel.util;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public final class Jdbc {
    private Jdbc() {}

    /**
     * Runs an INSERT and returns the auto-generated id.
     * Databases differ in what they report: H2 returns every column with a default (ID and CREATED_AT),
     * MySQL returns a single GENERATED_KEY. So we pick the "id" column when it is there.
     */
    public static long insertReturningId(JdbcTemplate jdbc, String sql, Object... args) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            return ps;
        }, holder);

        Map<String, Object> keys = holder.getKeys();
        if (keys == null || keys.isEmpty()) throw new IllegalStateException("Insert did not return a generated id");
        Object id = null;
        for (Map.Entry<String, Object> e : keys.entrySet()) {
            if ("id".equalsIgnoreCase(e.getKey())) id = e.getValue();
        }
        if (id == null && keys.size() == 1) id = keys.values().iterator().next();
        if (!(id instanceof Number)) throw new IllegalStateException("Could not read the generated id from " + keys);
        return ((Number) id).longValue();
    }
}
