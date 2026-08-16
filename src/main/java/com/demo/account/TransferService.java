package com.demo.account;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.account.dto.TransactionRequestDto;
import com.demo.audit.AuditService;
import com.demo.transaction.TransactionMapper;
import com.demo.transaction.TransactionRecord;
import com.demo.transaction.TransactionRecordService;
import com.demo.transaction.TransactionType;
import com.demo.transaction.dto.TransactionResponseDto;

@Service
public class TransferService {

        private final TransactionRecordService transactionRecordService;
        private final AccountService accountService;
        private final AuditService auditService;
        private final TransactionMapper transactionMapper;

        public TransferService(TransactionRecordService transactionRecordService, AccountService accountService,
                        AuditService auditService, TransactionMapper transactionMapper) {
                this.transactionRecordService = transactionRecordService;
                this.accountService = accountService;
                this.auditService = auditService;
                this.transactionMapper = transactionMapper;
        }

        @Transactional
        public TransactionResponseDto transfer(Long sourceAccountId, Long targetAccountId, long amount,
                        String idempotencyKey) {

                Optional<TransactionRecord> existingTransaction = transactionRecordService
                                .findByIdempotencyKey(idempotencyKey + "-OUT");

                if (existingTransaction.isPresent()) {

                        TransactionRecord existing = existingTransaction.get();
                        auditService.logAction("test-user-t", "TRANSFER",
                                        "Funds alaredy transferred successfully " + amount
                                                        + " from account ID: " + sourceAccountId + " to account ID: "
                                                        + targetAccountId);
                        return transactionMapper.toDuplicateResponse(existing);
                }

                if (sourceAccountId.equals(targetAccountId)) {
                        throw new IllegalArgumentException("Source and target accounts cannot be the same");
                }

                // Prevent deadlocks by ordering IDs (always lock lower ID first)
                Long firstId = Math.min(sourceAccountId, targetAccountId);
                Long secondId = Math.max(sourceAccountId, targetAccountId);

                Account firstAccount = accountService.getAccount(firstId);
                Account secondAccount = accountService.getAccount(secondId);

                Account sourceAccount = (sourceAccountId.equals(firstId)) ? firstAccount : secondAccount;
                Account targetAccount = (targetAccountId.equals(firstId)) ? firstAccount : secondAccount;

                // Perform domain operations
                sourceAccount.withdraw(amount);
                targetAccount.deposit(amount);

                TransactionRecord sourceRecord = transactionMapper.toEntity(
                                new TransactionRequestDto(amount),
                                sourceAccount,
                                idempotencyKey + "-OUT",
                                TransactionType.TRANSFER_OUT);

                TransactionRecord targetRecord = transactionMapper.toEntity(
                                new TransactionRequestDto(amount),
                                sourceAccount,
                                idempotencyKey + "-IN",
                                TransactionType.TRANSFER_IN);

                transactionRecordService.save(sourceRecord);
                transactionRecordService.save(targetRecord);
                auditService.logAction("test-user-t", "TRANSFER", "Funds transferred successfully " + amount
                                + " from account ID: " + sourceAccountId + " to account ID: " + targetAccountId);

                return transactionMapper.toResponseDto(sourceRecord, "Funds transferred successfully");

        }
}