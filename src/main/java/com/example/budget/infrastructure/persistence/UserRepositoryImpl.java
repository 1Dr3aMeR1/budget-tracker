package com.example.budget.infrastructure.persistence;

import com.example.budget.domain.model.User;
import com.example.budget.domain.repository.UserRepository;
import com.example.budget.infrastructure.persistence.jpa.UserEntity;
import com.example.budget.infrastructure.persistence.jpa.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash()
        );

        UserEntity saved = jpaRepository.save(entity);

        return new User(
                saved.getId(),
                saved.getUsername(),
                saved.getPasswordHash()
        );
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(e -> new User(
                        e.getId(),
                        e.getUsername(),
                        e.getPasswordHash()
                ));
    }

    @Override
    public Optional<User> findById(java.util.UUID id) {
        return jpaRepository.findById(id)
                .map(e -> new User(
                        e.getId(),
                        e.getUsername(),
                        e.getPasswordHash()
                ));
    }
}