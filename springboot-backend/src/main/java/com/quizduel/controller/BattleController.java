package com.quizduel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quizduel.dto.BattleDtos.BattleRow;
import com.quizduel.dto.BattleDtos.GenerateRequest;
import com.quizduel.dto.BattleDtos.GenerateResult;
import com.quizduel.dto.BattleDtos.SubmitAnswersRequest;
import com.quizduel.dto.BattleDtos.SubmitResponse;
import com.quizduel.security.AuthGuard;
import com.quizduel.service.BattleService;

@RestController
@RequestMapping("/api/battles")
public class BattleController {

    private static final String AUTH = "Authorization";

    private final BattleService battles;
    private final AuthGuard guard;

    public BattleController(BattleService battles, AuthGuard guard) {
        this.battles = battles;
        this.guard = guard;
    }

    // ---------- teacher ----------

    /** Today's Battles table. */
    @GetMapping("/today")
    public List<BattleRow> today(@RequestHeader(value = AUTH, required = false) String auth,
                                 @RequestParam(value = "sectionId", required = false) String sectionId) {
        return battles.today(guard.requireRole(auth, "teacher"), sectionId);
    }

    /** Battle Results Log: completed battles, newest first. */
    @GetMapping("/log")
    public List<BattleRow> log(@RequestHeader(value = AUTH, required = false) String auth,
                               @RequestParam(value = "sectionId", required = false) String sectionId,
                               @RequestParam(value = "limit", required = false) Integer limit) {
        return battles.log(guard.requireRole(auth, "teacher"), sectionId, limit);
    }

    /** Runs today's random matchmaking for a section. 201 if created, 200 if it already existed. */
    @PostMapping("/generate")
    public ResponseEntity<GenerateResult> generate(@RequestHeader(value = AUTH, required = false) String auth,
                                                   @RequestBody GenerateRequest body) {
        GenerateResult r = battles.generate(guard.requireRole(auth, "teacher"), body.sectionId());
        return ResponseEntity.status(r.alreadyExisted() ? HttpStatus.OK : HttpStatus.CREATED).body(r);
    }

    // ---------- student ----------

    /** The signed-in student's battles for today. */
    @GetMapping("/me/today")
    public List<BattleRow> myToday(@RequestHeader(value = AUTH, required = false) String auth) {
        return battles.myToday(guard.requireRole(auth, "student"));
    }

    /** Battle + questions. Correct answers are hidden from students until the battle is completed. */
    @GetMapping("/{id}")
    public Map<String, Object> detail(@RequestHeader(value = AUTH, required = false) String auth,
                                      @PathVariable("id") long id) {
        return battles.detail(guard.require(auth), id);
    }

    /** POST /api/battles/{id}/answers  {answers:[{questionId, selected:"A".."D", timeTaken}]} */
    @PostMapping("/{id}/answers")
    public SubmitResponse submit(@RequestHeader(value = AUTH, required = false) String auth,
                                 @PathVariable("id") long id, @RequestBody SubmitAnswersRequest body) {
        return battles.submitAnswers(guard.requireRole(auth, "student"), id, body.answers());
    }
}
