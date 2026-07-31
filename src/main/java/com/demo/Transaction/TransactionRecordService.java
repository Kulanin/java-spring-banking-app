package com.demo.Transaction;

import java.util.Optional;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TransactionRecordService {

    final private TransactionRecordRepository transactionRecordRepository;

    public TransactionRecordService(TransactionRecordRepository transactionRecordRepository) {
        this.transactionRecordRepository = transactionRecordRepository;
    }

    public Optional<TransactionRecord> findById(Long id) {
        return transactionRecordRepository.findById(id);
    }

    public TransactionRecord save(TransactionRecord transactionRecord) {
        return transactionRecordRepository.save(transactionRecord);
    }

    public Optional<TransactionRecord> findByIdempotencyKey(String key) {
        return transactionRecordRepository.findByIdempotencyKey(key);
    }

}
