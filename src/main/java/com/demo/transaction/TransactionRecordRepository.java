package com.demo.transaction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {
    Optional<TransactionRecord> findByIdempotencyKey(String idempotencyKey);

    List<TransactionRecord> findByAccount_Id(Long accountId);
}