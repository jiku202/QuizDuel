package com.quizduel.security;

/** The signed-in user, taken from the JWT. */
public record CurrentUser(long id, String role) {
    public boolean isTeacher() { return "teacher".equals(role); }
    public boolean isStudent() { return "student".equals(role); }
}
