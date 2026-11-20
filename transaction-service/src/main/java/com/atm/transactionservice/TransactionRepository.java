package com.atm.transactionservice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByOriginalTransactionId(Long originalTransactionId);

    @Query("""
            SELECT t FROM Transaction t
            WHERE (CAST(:accountId AS Long) IS NULL OR
                   t.accountId = :accountId OR
                   t.relatedAccountId = :accountId)
            AND (CAST(:startDate AS LocalDateTime) IS NULL OR t.timestamp >= :startDate)
            AND (CAST(:endDate AS LocalDateTime) IS NULL OR t.timestamp <= :endDate)
            ORDER BY t.timestamp DESC
            """)
    Page<Transaction> findTransactions(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}