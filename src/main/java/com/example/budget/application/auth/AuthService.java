package com.example.budget.application.auth;

import com.example.budget.domain.model.User;
import com.example.budget.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

    public User login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не зарегистрирован"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Неверный пороль");
        }

        return user;
    }
}