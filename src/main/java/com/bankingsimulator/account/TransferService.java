package com.bankingsimulator.account;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingsimulator.account.dto.TransactionRequestDto;
import com.bankingsimulator.outbox.OutboxService;
import com.bankingsimulator.rabbitmq.RabbitMQConfig;
import com.bankingsimulator.transaction.TransactionMapper;
import com.bankingsimulator.transaction.TransactionRecord;
import com.bankingsimulator.transaction.TransactionRecordService;
import com.bankingsimulator.transaction.TransactionType;
import com.bankingsimulator.transaction.dto.TransactionResponseDto;

@Service
public class TransferService {

        private final TransactionRecordService transactionRecordService;
        private final AccountService accountService;
        private final OutboxService outboxService;
        private final TransactionMapper transactionMapper;

        public TransferService(TransactionRecordService transactionRecordService, AccountService accountService,
                        OutboxService outboxService, TransactionMapper transactionMapper) {
                this.transactionRecordService = transactionRecordService;
                this.accountService = accountService;
                this.outboxService = outboxService;
                this.transactionMapper = transactionMapper;
        }

        @Transactional
        public TransactionResponseDto transfer(Long sourceAccountId, Long targetAccountId, long amount,
                        String idempotencyKey) {
                if (sourceAccountId.equals(targetAccountId)) {
                        throw new IllegalArgumentException("Source and target accounts cannot be the same");
                }

                Optional<TransactionRecord> existingTransaction = transactionRecordService
                                .findByIdempotencyKey(idempotencyKey + "-OUT");

                if (existingTransaction.isPresent()) {

                        TransactionRecord existing = existingTransaction.get();
                        return transactionMapper.toDuplicateResponse(existing);
                }

                // Prevent deadlocks by ordering IDs (always lock lower ID first)
                Long firstId = Math.min(sourceAccountId, targetAccountId);
                Long secondId = Math.max(sourceAccountId, targetAccountId);

                Account firstAccount = accountService.getAccountForUpdate(firstId);
                Account secondAccount = accountService.getAccountForUpdate(secondId);

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
                                targetAccount,
                                idempotencyKey + "-IN",
                                TransactionType.TRANSFER_IN);

                transactionRecordService.save(sourceRecord);
                transactionRecordService.save(targetRecord);

                outboxService.stageTransactionEvent(
                                sourceAccountId,
                                amount,
                                TransactionType.TRANSFER_OUT,
                                idempotencyKey + "-OUT",
                                sourceRecord.getId(),
                                RabbitMQConfig.TX_TRANSFER_OUT_KEY);

                outboxService.stageTransactionEvent(
                                targetAccountId,
                                amount,
                                TransactionType.TRANSFER_IN,
                                idempotencyKey + "-IN",
                                targetRecord.getId(),
                                RabbitMQConfig.TX_TRANSFER_IN_KEY);

                return transactionMapper.toResponseDto(sourceRecord, "Funds transferred successfully");

        }
}