package com.quizduel.service;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Ranks are worked out from a student's points in a class, using the point-threshold rows of the badges table
 * as tiers. Nothing is stored, so a rank goes up and down together with the points.
 */
@Service
public class RankService {

    public record Tier(long id, String name, String description, int threshold) {}

    private final JdbcTemplate jdbc;

    public RankService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Tiers from lowest to highest. */
    public List<Tier> tiers() {
        return jdbc.query(
                "SELECT id, name, description, points_threshold FROM badges WHERE points_threshold IS NOT NULL ORDER BY points_threshold",
                (rs, i) -> new Tier(rs.getLong("id"), rs.getString("name"), rs.getString("description"), rs.getInt("points_threshold")));
    }

    /** Name of the highest tier reached with these points. */
    public static String rankFor(List<Tier> tiers, int points) {
        String name = "Unranked";
        for (Tier t : tiers) {
            if (points >= t.threshold()) name = t.name();
        }
        return name;
    }

    public String rankFor(int points) {
        return rankFor(tiers(), points);
    }
}
