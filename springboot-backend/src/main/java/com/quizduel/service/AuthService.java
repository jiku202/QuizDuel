package com.quizduel.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.quizduel.dto.AuthDtos.AuthResponse;
import com.quizduel.dto.AuthDtos.LoginRequest;
import com.quizduel.dto.AuthDtos.MeResponse;
import com.quizduel.dto.AuthDtos.RegisterRequest;
import com.quizduel.dto.AuthDtos.UserDto;
import com.quizduel.exception.ApiException;
import com.quizduel.security.CurrentUser;
import com.quizduel.security.JwtService;
import com.quizduel.util.Jdbc;
import com.quizduel.util.Validation;

@Service
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String USER_COLUMNS = "id, name, email, role, school, total_points, streak";

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(JdbcTemplate jdbc, PasswordEncoder encoder, JwtService jwt) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** role is "teacher" (default) or "student". */
    public AuthResponse register(RegisterRequest req) {
        Validation.required("name", req.name(), "email", req.email(), "password", req.password());
        String email = req.email().trim().toLowerCase();
        String role = "student".equals(req.role()) ? "student" : "teacher";

        if (!EMAIL.matcher(email).matches()) throw ApiException.badRequest("Invalid email address");
        if (req.password().length() < 6) throw ApiException.badRequest("Password must be at least 6 characters");
        Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        if (exists != null && exists > 0) throw ApiException.conflict("Email is already registered");

        long id = Jdbc.insertReturningId(jdbc,
                "INSERT INTO users (name, email, password_hash, role, school) VALUES (?, ?, ?, ?, ?)",
                req.name().trim(), email, encoder.encode(req.password()), role, req.school());
        UserDto user = findById(id);
        return new AuthResponse(jwt.sign(user.id(), user.role()), user);
    }

    public AuthResponse login(LoginRequest req) {
        Validation.required("email", req.email(), "password", req.password());
        String email = req.email().trim().toLowerCase();
        List<String> hashes = jdbc.queryForList("SELECT password_hash FROM users WHERE email = ?", String.class, email);
        if (hashes.isEmpty() || !encoder.matches(req.password(), hashes.get(0))) {
            throw ApiException.unauthorized("Incorrect email or password");
        }
        long id = jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
        UserDto user = findById(id);
        return new AuthResponse(jwt.sign(user.id(), user.role()), user);
    }

    public MeResponse me(CurrentUser current) {
        return new MeResponse(findById(current.id()));
    }

    private UserDto findById(long id) {
        List<UserDto> rows = jdbc.query("SELECT " + USER_COLUMNS + " FROM users WHERE id = ?",
                (rs, i) -> new UserDto(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                        rs.getString("role"), rs.getString("school"), rs.getInt("total_points"), rs.getInt("streak")),
                id);
        if (rows.isEmpty()) throw ApiException.notFound("User not found");
        return rows.get(0);
    }
}
