package com.example.budget.infrastructure.persistence.repository;

import com.example.budget.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            UUID userId,
            OffsetDateTime from,
            OffsetDateTime to
    );
}
