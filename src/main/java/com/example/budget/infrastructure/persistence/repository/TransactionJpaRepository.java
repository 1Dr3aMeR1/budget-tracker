package com.example.budget.infrastructure.persistence.repository;

import com.example.budget.infrastructure.persistence.entity.TransactionEntity;
import com.example.budget.domain.model.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    interface ExpenseAggRow {
        UUID getCategoryId();
        BigDecimal getTotal();
    }

    @Query("""
        select t.categoryId as categoryId, sum(t.amount) as total
        from TransactionEntity t
        where t.userId = :userId
          and t.type = :type
          and t.occurredAt >= :from and t.occurredAt <= :to
        group by t.categoryId
        order by sum(t.amount) desc
    """)
    List<ExpenseAggRow> sumByCategory(
            @Param("userId") UUID userId,
            @Param("type") CategoryType type,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    List<TransactionEntity> findAllByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(
            UUID userId,
            OffsetDateTime from,
            OffsetDateTime to
    );

}

