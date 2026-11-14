package com.bankingsimulator.events;

import java.math.BigDecimal;
import java.time.Instant;

import com.bankingsimulator.transaction.TransactionType;

public record TransactionCompletedEvent(
                Long accountId,
                BigDecimal amount,
                TransactionType transactionType,
                String idempotencyKey,
                String transactionId,
                Instant occurredAt) {
}