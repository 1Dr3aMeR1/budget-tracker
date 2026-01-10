package com.example.budget.api.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseByCategoryResponse(
        UUID categoryId,
        String categoryName,
        BigDecimal totalAmount
) {}

