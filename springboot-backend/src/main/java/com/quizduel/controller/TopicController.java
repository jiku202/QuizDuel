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

import com.quizduel.dto.TopicDtos.CopyTopicRequest;
import com.quizduel.dto.TopicDtos.TopicDto;
import com.quizduel.dto.TopicDtos.TopicRequest;
import com.quizduel.security.AuthGuard;
import com.quizduel.service.TopicService;

/** Topics inside a section's question bank: teachers only. */
@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private static final String AUTH = "Authorization";

    private final TopicService topics;
    private final AuthGuard guard;

    public TopicController(TopicService topics, AuthGuard guard) {
        this.topics = topics;
        this.guard = guard;
    }

    /** GET /api/topics?sectionId=  (no sectionId = topics of all the teacher's sections) */
    @GetMapping
    public List<TopicDto> list(@RequestHeader(value = AUTH, required = false) String auth,
                               @RequestParam(value = "sectionId", required = false) String sectionId) {
        return topics.list(guard.requireRole(auth, "teacher"), sectionId);
    }

    /** POST /api/topics {sectionId, name} */
    @PostMapping
    public ResponseEntity<TopicDto> create(@RequestHeader(value = AUTH, required = false) String auth,
                                           @RequestBody TopicRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(topics.create(guard.requireRole(auth, "teacher"), body));
    }

    /** PUT /api/topics/{id} {name} */
    @PutMapping("/{id}")
    public TopicDto rename(@RequestHeader(value = AUTH, required = false) String auth,
                           @PathVariable("id") long id, @RequestBody TopicRequest body) {
        return topics.rename(guard.requireRole(auth, "teacher"), id, body);
    }

    /** POST /api/topics/{id}/copy {sectionId, copyQuestions}: add an existing topic to another section. */
    @PostMapping("/{id}/copy")
    public ResponseEntity<TopicDto> copy(@RequestHeader(value = AUTH, required = false) String auth,
                                         @PathVariable("id") long id, @RequestBody CopyTopicRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(topics.copyTo(guard.requireRole(auth, "teacher"), id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader(value = AUTH, required = false) String auth,
                                       @PathVariable("id") long id) {
        topics.delete(guard.requireRole(auth, "teacher"), id);
        return ResponseEntity.noContent().build();
    }
}
