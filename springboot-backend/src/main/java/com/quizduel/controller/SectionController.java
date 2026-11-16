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
import org.springframework.web.bind.annotation.RestController;

import com.quizduel.dto.SectionDtos.AddMembersRequest;
import com.quizduel.dto.SectionDtos.AddMembersResponse;
import com.quizduel.dto.SectionDtos.CreateSectionRequest;
import com.quizduel.dto.SectionDtos.JoinResponse;
import com.quizduel.dto.SectionDtos.JoinSectionRequest;
import com.quizduel.dto.SectionDtos.RosterEntry;
import com.quizduel.dto.SectionDtos.SectionDto;
import com.quizduel.dto.SectionDtos.UpdateSectionRequest;
import com.quizduel.security.AuthGuard;
import com.quizduel.service.SectionService;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    private static final String AUTH = "Authorization";

    private final SectionService sections;
    private final AuthGuard guard;

    public SectionController(SectionService sections, AuthGuard guard) {
        this.sections = sections;
        this.guard = guard;
    }

    /** Teacher: sections they own. Student: sections they joined. */
    @GetMapping
    public List<SectionDto> list(@RequestHeader(value = AUTH, required = false) String auth) {
        return sections.list(guard.require(auth));
    }

    /** Creates a section and its join code. */
    @PostMapping
    public ResponseEntity<SectionDto> create(@RequestHeader(value = AUTH, required = false) String auth,
                                             @RequestBody CreateSectionRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sections.create(guard.requireRole(auth, "teacher"), body));
    }

    /** Student joins a section with its code. 201 if newly joined, 200 if already in. */
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> join(@RequestHeader(value = AUTH, required = false) String auth,
                                             @RequestBody JoinSectionRequest body) {
        JoinResponse r = sections.join(guard.requireRole(auth, "student"), body.joinCode());
        return ResponseEntity.status(r.joined() ? HttpStatus.CREATED : HttpStatus.OK).body(r);
    }

    /** Edit a section; also used by the Settings page (battleTime, weekendBattles). */
    @PutMapping("/{id}")
    public SectionDto update(@RequestHeader(value = AUTH, required = false) String auth,
                             @PathVariable("id") long id, @RequestBody UpdateSectionRequest body) {
        return sections.update(guard.requireRole(auth, "teacher"), id, body);
    }

    @GetMapping("/{id}/roster")
    public List<RosterEntry> roster(@RequestHeader(value = AUTH, required = false) String auth,
                                    @PathVariable("id") long id) {
        return sections.roster(guard.requireRole(auth, "teacher"), id);
    }

    /** Teacher deletes a section (and its roster, questions and battles). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader(value = AUTH, required = false) String auth,
                                       @PathVariable("id") long id) {
        sections.delete(guard.requireRole(auth, "teacher"), id);
        return ResponseEntity.noContent().build();
    }

    /** Teacher adds students to a section by email. */
    @PostMapping("/{id}/roster")
    public AddMembersResponse addMembers(@RequestHeader(value = AUTH, required = false) String auth,
                                         @PathVariable("id") long id,
                                         @RequestBody AddMembersRequest body) {
        return sections.addMembers(guard.requireRole(auth, "teacher"), id, body.emails());
    }

    /** Teacher removes a student from a section. */
    @DeleteMapping("/{id}/roster/{studentId}")
    public ResponseEntity<Void> removeMember(@RequestHeader(value = AUTH, required = false) String auth,
                                             @PathVariable("id") long id,
                                             @PathVariable("studentId") long studentId) {
        sections.removeMember(guard.requireRole(auth, "teacher"), id, studentId);
        return ResponseEntity.noContent().build();
    }
}
