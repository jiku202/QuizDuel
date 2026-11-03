package com.quizduel.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quizduel.dto.QuestionDtos.QuestionDto;
import com.quizduel.dto.QuestionDtos.QuestionRequest;
import com.quizduel.security.AuthGuard;
import com.quizduel.service.QuestionService;

/** Question bank: teachers only. */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private static final String AUTH = "Authorization";

    private final QuestionService questions;
    private final AuthGuard guard;

    public QuestionController(QuestionService questions, AuthGuard guard) {
        this.questions = questions;
        this.guard = guard;
    }

    /** GET /api/questions?sectionId=&topic= */
    @GetMapping
    public List<QuestionDto> list(@RequestHeader(value = AUTH, required = false) String auth,
                                  @RequestParam(value = "sectionId", required = false) String sectionId,
                                  @RequestParam(value = "topic", required = false) String topic) {
        return questions.list(guard.requireRole(auth, "teacher"), sectionId, topic);
    }

    /** GET /api/questions/topics?sectionId= */
    @GetMapping("/topics")
    public List<String> topics(@RequestHeader(value = AUTH, required = false) String auth,
                               @RequestParam(value = "sectionId", required = false) String sectionId) {
        return questions.topics(guard.requireRole(auth, "teacher"), sectionId);
    }

    /** POST /api/questions {sectionId, topic, text, choices:{A,B,C,D}, correctAnswer, difficulty?} */
    @PostMapping
    public ResponseEntity<QuestionDto> create(@RequestHeader(value = AUTH, required = false) String auth,
                                              @RequestBody QuestionRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questions.create(guard.requireRole(auth, "teacher"), body));
    }

    @PutMapping("/{id}")
    public QuestionDto update(@RequestHeader(value = AUTH, required = false) String auth,
                              @PathVariable("id") long id, @RequestBody QuestionRequest body) {
        return questions.update(guard.requireRole(auth, "teacher"), id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader(value = AUTH, required = false) String auth,
                                       @PathVariable("id") long id) {
        questions.delete(guard.requireRole(auth, "teacher"), id);
        return ResponseEntity.noContent().build();
    }
}
