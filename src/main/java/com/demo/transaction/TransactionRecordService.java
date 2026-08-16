package com.demo.transaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.demo.transaction.dto.TransactionStatementDto;

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

    @Transactional()
    public List<TransactionStatementDto> getStatement(Long accountId) {
        List<TransactionRecord> records = transactionRecordRepository.findByAccount_Id(accountId);

        return records.stream()
                .map(record -> new TransactionStatementDto(
                        record.getId(),
                        record.getAmount(),
                        record.getBalanceAfter(),
                        record.getCreatedAt(),
                        record.getType(),
                        record.getAccountName()))
                .collect(Collectors.toList());
    }

}
