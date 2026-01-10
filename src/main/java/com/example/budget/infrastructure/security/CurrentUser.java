package com.example.budget.infrastructure.security;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class CurrentUser {
    private CurrentUser() {}

    public static UUID id() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthenticated");
        }
        if (auth.getPrincipal() instanceof JwtAuthFilter.AuthPrincipal p) {
            return p.userId();
        }
        throw new RuntimeException("Unsupported principal: " + auth.getPrincipal().getClass());
    }

    public static String username() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) throw new RuntimeException("Unauthenticated");
        if (auth.getPrincipal() instanceof JwtAuthFilter.AuthPrincipal p) return p.username();
        throw new RuntimeException("Unsupported principal");
    }
}
