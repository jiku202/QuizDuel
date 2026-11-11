package com.quizduel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quizduel.dto.AuthDtos.AuthResponse;
import com.quizduel.dto.AuthDtos.LoginRequest;
import com.quizduel.dto.AuthDtos.MeResponse;
import com.quizduel.dto.AuthDtos.RegisterRequest;
import com.quizduel.security.AuthGuard;
import com.quizduel.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthGuard guard;

    public AuthController(AuthService authService, AuthGuard guard) {
        this.authService = authService;
        this.guard = guard;
    }

    /** POST /api/auth/register  {name, email, password, school?, role?}  role = "teacher" (default) | "student" */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(body));
    }

    /** POST /api/auth/login  {email, password} */
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest body) {
        return authService.login(body);
    }

    /** GET /api/auth/me */
    @GetMapping("/me")
    public MeResponse me(@RequestHeader(value = "Authorization", required = false) String auth) {
        return authService.me(guard.require(auth));
    }
}
