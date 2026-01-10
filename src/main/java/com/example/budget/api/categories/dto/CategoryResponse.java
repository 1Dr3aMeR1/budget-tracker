package com.example.budget.api.categories.dto;

import com.example.budget.domain.model.CategoryType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID userId,
        String name,
        CategoryType type,
        OffsetDateTime createdAt
) {}