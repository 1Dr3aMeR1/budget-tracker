package com.example.budget.api.auth;

import com.example.budget.api.auth.dto.LoginRequest;
import com.example.budget.api.auth.dto.RegisterRequest;
import com.example.budget.application.auth.AuthService;
import com.example.budget.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(@Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req.username(), req.password());
        return "registered userId=" + user.getId();
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest req) {
        User user = authService.login(req.username(), req.password());
        return "ok userId=" + user.getId();
    }
}