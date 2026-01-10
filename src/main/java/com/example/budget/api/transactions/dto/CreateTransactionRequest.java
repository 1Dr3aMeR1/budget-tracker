package com.example.budget.api.transactions.dto;

import com.example.budget.domain.model.CategoryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTransactionRequest(
        UUID categoryId,
        @NotNull @Positive BigDecimal amount,
        @NotNull CategoryType type,
        @NotNull OffsetDateTime occurredAt,
        @Size(max = 255) String note
) {}

