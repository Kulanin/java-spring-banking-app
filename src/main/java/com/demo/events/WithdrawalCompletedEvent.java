package com.demo.events;

import java.math.BigDecimal;
import java.time.Instant;

public record WithdrawalCompletedEvent(
                Long accountId,
                BigDecimal amount,
                String idempotencyKey,
                String transactionId,
                Instant occurredAt) {
}