package com.demo.transaction;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demo.transaction.dto.TransactionStatementDto;

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

    @Transactional(readOnly = true)
    public Page<TransactionStatementDto> getTransactions(
            Long accountId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        Specification<TransactionRecord> spec = TransactionSpecification.filterBy(
                accountId, type, startDate, endDate);

        return transactionRecordRepository.findAll(spec, pageable)
                .map(record -> new TransactionStatementDto(
                        record.getId(),
                        record.getAmount(),
                        record.getBalanceAfter(),
                        record.getCreatedAt(),
                        record.getType(),
                        record.getAccountName()));
    }

}
