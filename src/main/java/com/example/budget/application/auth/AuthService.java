
package com.example.budget.application.auth;

import com.example.budget.domain.model.User;
import com.example.budget.domain.repository.UserRepository;
import com.example.budget.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(String username, String rawPassword) {
        String hash = passwordEncoder.encode(rawPassword);

        User user = new User(
                UUID.randomUUID(),
                username,
                hash
        );

        return userRepository.save(user);
    }

    public String loginAndGetToken(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не зарегистрирован"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Неверный пароль");
        }

        return jwtService.generateToken(user.getId(), user.getUsername());
    }
}