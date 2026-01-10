package com.example.budget.api.categories.dto;

import com.example.budget.domain.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull CategoryType type
) {}