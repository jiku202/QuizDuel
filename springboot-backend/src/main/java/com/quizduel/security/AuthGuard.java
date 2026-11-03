package com.quizduel.security;

import org.springframework.stereotype.Component;

import com.quizduel.exception.ApiException;

/**
 * Reads the "Authorization: Bearer <token>" header. Controllers call require() / requireRole()
 * as the first line of each endpoint.
 */
@Component
public class AuthGuard {

    private final JwtService jwt;

    public AuthGuard(JwtService jwt) {
        this.jwt = jwt;
    }

    public CurrentUser require(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Missing token");
        }
        return jwt.verify(authorizationHeader.substring(7));
    }

    public CurrentUser requireRole(String authorizationHeader, String role) {
        CurrentUser user = require(authorizationHeader);
        if (!role.equals(user.role())) throw ApiException.forbidden(role + " access only");
        return user;
    }
}
