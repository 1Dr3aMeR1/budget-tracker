package com.example.budget.domain.repository;

import com.example.budget.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findById(UUID id);
}