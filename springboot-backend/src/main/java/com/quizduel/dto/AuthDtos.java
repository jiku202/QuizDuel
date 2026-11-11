package com.quizduel.dto;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(String name, String email, String password, String school, String role) {}

    public record LoginRequest(String email, String password) {}

    public record UserDto(long id, String name, String email, String role, String school, int totalPoints, int streak) {}

    public record AuthResponse(String token, UserDto user) {}

    public record MeResponse(UserDto user) {}
}
