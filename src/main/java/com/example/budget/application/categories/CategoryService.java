package com.example.budget.application.categories;

import com.example.budget.api.categories.dto.CategoryResponse;
import com.example.budget.api.categories.dto.CreateCategoryRequest;
import com.example.budget.api.categories.dto.UpdateCategoryRequest;
import com.example.budget.infrastructure.persistence.entity.CategoryEntity;
import com.example.budget.infrastructure.persistence.repository.CategoryJpaRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryJpaRepository repo;

    public CategoryService(CategoryJpaRepository repo) {
        this.repo = repo;
    }

    public CategoryResponse create(UUID userId, CreateCategoryRequest req) {
        CategoryEntity e = new CategoryEntity();
        e.setId(UUID.randomUUID());
        e.setUserId(userId);
        e.setName(req.name());
        e.setType(req.type());
        e.setCreatedAt(OffsetDateTime.now());
        return toResponse(repo.save(e));
    }

    public List<CategoryResponse> list(UUID userId) {
        return repo.findAllByUserId(userId).stream().map(this::toResponse).toList();
    }

    public CategoryResponse update(UUID userId, UUID id, UpdateCategoryRequest req) {
        CategoryEntity e = repo.findById(id).orElseThrow();

        if (!e.getUserId().equals(userId)) {
            throw new RuntimeException("Нет доступа");
        }

        e.setName(req.name());
        return toResponse(repo.save(e));
    }

    public void delete(UUID userId, UUID id) {
        CategoryEntity e = repo.findById(id).orElseThrow();

        if (!e.getUserId().equals(userId)) {
            throw new RuntimeException("Нет доступа");
        }

        repo.deleteById(id);
    }

    private CategoryResponse toResponse(CategoryEntity e) {
        return new CategoryResponse(e.getId(), e.getUserId(), e.getName(), e.getType(), e.getCreatedAt());
    }
}