package com.example.budget.infrastructure.config;

import com.example.budget.application.auth.AuthService;
import com.example.budget.domain.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthService authService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return new AuthService(userRepository, passwordEncoder);
    }
}