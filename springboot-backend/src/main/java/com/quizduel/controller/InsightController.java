package com.quizduel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quizduel.dto.InsightDtos.Analytics;
import com.quizduel.dto.InsightDtos.Dashboard;
import com.quizduel.dto.InsightDtos.LeaderboardEntry;
import com.quizduel.security.AuthGuard;
import com.quizduel.service.InsightService;

/** Dashboard, analytics, leaderboard and badges. */
@RestController
@RequestMapping("/api")
public class InsightController {

    private static final String AUTH = "Authorization";

    private final InsightService insights;
    private final AuthGuard guard;

    public InsightController(InsightService insights, AuthGuard guard) {
        this.insights = insights;
        this.guard = guard;
    }

    @GetMapping("/dashboard")
    public Dashboard dashboard(@RequestHeader(value = AUTH, required = false) String auth,
                               @RequestParam(value = "sectionId", required = false) String sectionId) {
        return insights.dashboard(guard.requireRole(auth, "teacher"), sectionId);
    }

    @GetMapping("/analytics")
    public Analytics analytics(@RequestHeader(value = AUTH, required = false) String auth,
                               @RequestParam(value = "sectionId", required = false) String sectionId) {
        return insights.analytics(guard.requireRole(auth, "teacher"), sectionId);
    }

    /**
     * Teacher: ?sectionIds=1,2 for chosen sections, nothing for all of their sections.
     * Student: ?sectionId= of a section they joined.
     */
    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> leaderboard(@RequestHeader(value = AUTH, required = false) String auth,
                                              @RequestParam(value = "sectionId", required = false) String sectionId,
                                              @RequestParam(value = "sectionIds", required = false) String sectionIds) {
        return insights.leaderboard(guard.require(auth), sectionId, sectionIds);
    }

    /** Rank tiers (automatic, based on points in a class). */
    @GetMapping("/badges")
    public List<Map<String, Object>> badges(@RequestHeader(value = AUTH, required = false) String auth) {
        return insights.badges(guard.require(auth));
    }
}
