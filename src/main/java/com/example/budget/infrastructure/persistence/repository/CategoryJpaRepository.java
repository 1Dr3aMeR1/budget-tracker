package com.example.budget.infrastructure.persistence.repository;

import com.example.budget.domain.model.CategoryType;
import com.example.budget.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findAllByUserIdAndType(UUID userId, CategoryType type);
    List<CategoryEntity> findAllByUserId(UUID userId);
}
