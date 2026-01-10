package com.example.budget.application.analytics;

import com.example.budget.api.analytics.dto.ExpenseByCategoryResponse;
import com.example.budget.domain.model.CategoryType;
import com.example.budget.infrastructure.persistence.repository.CategoryJpaRepository;
import com.example.budget.infrastructure.persistence.repository.TransactionJpaRepository;
import com.example.budget.infrastructure.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final TransactionJpaRepository txRepo;
    private final CategoryJpaRepository categoryRepo;

    public AnalyticsService(TransactionJpaRepository txRepo, CategoryJpaRepository categoryRepo) {
        this.txRepo = txRepo;
        this.categoryRepo = categoryRepo;
    }

    @Transactional(readOnly = true)
    public List<ExpenseByCategoryResponse> expensesByCategory(OffsetDateTime from, OffsetDateTime to) {
        UUID userId = CurrentUser.id();

        var rows = txRepo.sumByCategory(userId, CategoryType.EXPENSE, from, to);

        var categories = categoryRepo.findAllByUserIdAndType(userId, CategoryType.EXPENSE);
        Map<UUID, String> idToName = categories.stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));

        return rows.stream()
                .map(r -> new ExpenseByCategoryResponse(
                        r.getCategoryId(),
                        r.getCategoryId() == null ? "Другое" : idToName.getOrDefault(r.getCategoryId(), "Неизвестная категория"),
                        r.getTotal()
                ))
                .toList();
    }
}
