package com.demo.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.Transaction.TransactionRecord;
import com.demo.Transaction.TransactionRecordService;
import com.demo.Transaction.TransactionType;
import com.demo.Transaction.dto.TransactionResponse;
import com.demo.Transaction.dto.TransactionStatus;

@Service
public class TransferService {

    private final TransactionRecordService transactionRecordService;
    private final AccountService accountService;

    public TransferService(TransactionRecordService transactionRecordService, AccountService accountService) {
        this.transactionRecordService = transactionRecordService;
        this.accountService = accountService;
    }

    @Transactional
    public TransactionResponse transfer(Long sourceAccountId, Long targetAccountId, long amount,
            String idempotencyKey) {

        if (transactionRecordService.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return new TransactionResponse(TransactionStatus.ALREADY_PROCESSED, 0,
                    "This transfer was already completed");
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

        // Save transaction records for audit trails
        TransactionRecord sourceRecord = new TransactionRecord(idempotencyKey + "-OUT", sourceAccountId, -amount,
                TransactionType.TRANSFER);
        TransactionRecord targetRecord = new TransactionRecord(idempotencyKey + "-IN", targetAccountId, amount,
                TransactionType.TRANSFER);

        transactionRecordService.save(sourceRecord);
        transactionRecordService.save(targetRecord);

        return new TransactionResponse(TransactionStatus.SUCCESS, sourceAccount.getBalance(), "Transfer successful");
    }
}