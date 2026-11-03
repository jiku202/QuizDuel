package com.quizduel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quizduel.dto.InsightDtos.Analytics;
import com.quizduel.dto.InsightDtos.CreateBadgeRequest;
import com.quizduel.dto.InsightDtos.Dashboard;
import com.quizduel.dto.InsightDtos.LeaderboardEntry;
import com.quizduel.dto.SectionDtos.BadgeDto;
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

    /** Teacher: own sections. Student: must pass a section they joined. */
    @GetMapping("/leaderboard")
    public List<LeaderboardEntry> leaderboard(@RequestHeader(value = AUTH, required = false) String auth,
                                              @RequestParam(value = "sectionId", required = false) String sectionId) {
        return insights.leaderboard(guard.require(auth), sectionId);
    }

    /** All badges; students also see "earned". */
    @GetMapping("/badges")
    public List<Map<String, Object>> badges(@RequestHeader(value = AUTH, required = false) String auth) {
        return insights.badges(guard.require(auth));
    }

    /** Teacher creates a custom badge for manual awards. */
    @PostMapping("/badges")
    public ResponseEntity<BadgeDto> createBadge(@RequestHeader(value = AUTH, required = false) String auth,
                                                @RequestBody CreateBadgeRequest body) {
        guard.requireRole(auth, "teacher");
        return ResponseEntity.status(HttpStatus.CREATED).body(insights.createBadge(body.name(), body.description()));
    }
}
