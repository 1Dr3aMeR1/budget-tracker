package com.example.budget.application.transactions;

import com.example.budget.api.transactions.dto.CreateTransactionRequest;
import com.example.budget.api.transactions.dto.TransactionResponse;
import com.example.budget.domain.model.CategoryType;
import com.example.budget.infrastructure.persistence.entity.CategoryEntity;
import com.example.budget.infrastructure.persistence.entity.TransactionEntity;
import com.example.budget.infrastructure.persistence.repository.CategoryJpaRepository;
import com.example.budget.infrastructure.persistence.repository.TransactionJpaRepository;
import com.example.budget.infrastructure.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionJpaRepository txRepo;
    private final CategoryJpaRepository categoryRepo;

    public TransactionService(TransactionJpaRepository txRepo, CategoryJpaRepository categoryRepo) {
        this.txRepo = txRepo;
        this.categoryRepo = categoryRepo;
    }

    @Transactional
    public TransactionResponse create(CreateTransactionRequest req) {
        UUID userId = CurrentUser.id();

        UUID categoryId = req.categoryId();

        if (categoryId == null) {
            if (req.type() == CategoryType.EXPENSE) {
                categoryId = getOrCreateOtherExpenseCategory(userId).getId();
            } else {
                categoryId = null;
            }
        } else {
            CategoryEntity cat = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Категория не найдена"));

            if (!cat.getUserId().equals(userId)) {
                throw new RuntimeException("Категория принадлежит другому пользователю");
            }
            if (cat.getType() != req.type()) {
                throw new RuntimeException("Тип категории не соответствует типу транзакции");
            }
        }

        TransactionEntity e = new TransactionEntity();
        e.setId(UUID.randomUUID());
        e.setUserId(userId);
        e.setCategoryId(categoryId);
        e.setAmount(req.amount());
        e.setType(req.type());
        e.setOccurredAt(req.occurredAt());
        e.setNote(req.note());
        e.setCreatedAt(OffsetDateTime.now());

        return toResponse(txRepo.save(e));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> list(OffsetDateTime from, OffsetDateTime to) {
        UUID userId = CurrentUser.id();
        return txRepo.findAllByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(userId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TransactionResponse toResponse(TransactionEntity e) {
        return new TransactionResponse(
                e.getId(),
                e.getCategoryId(),
                e.getAmount(),
                e.getType(),
                e.getOccurredAt(),
                e.getNote()
        );
    }

    private CategoryEntity getOrCreateOtherExpenseCategory(UUID userId) {
        var existing = categoryRepo.findAllByUserIdAndType(userId, CategoryType.EXPENSE).stream()
                .filter(c -> "Другое".equalsIgnoreCase(c.getName()) || "Other".equalsIgnoreCase(c.getName()))
                .findFirst();

        if (existing.isPresent()) return existing.get();

        CategoryEntity e = new CategoryEntity();
        e.setId(UUID.randomUUID());
        e.setUserId(userId);
        e.setName("Другое");
        e.setType(CategoryType.EXPENSE);
        e.setCreatedAt(OffsetDateTime.now());
        return categoryRepo.save(e);
    }
}

