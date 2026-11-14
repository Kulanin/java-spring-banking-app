package com.demo.transaction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRecordRepository
        extends JpaRepository<TransactionRecord, Long>, JpaSpecificationExecutor<TransactionRecord> {

    Optional<TransactionRecord> findByIdempotencyKey(String idempotencyKey);
}
