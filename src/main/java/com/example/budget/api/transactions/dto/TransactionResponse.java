package com.example.budget.api.transactions.dto;

import com.example.budget.domain.model.CategoryType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID categoryId,
        BigDecimal amount,
        CategoryType type,
        OffsetDateTime occurredAt,
        String note
) {}

