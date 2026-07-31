package com.demo.Transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransferRequest {
    @NotNull(message = "Traget account ID is required")
    private Long targetAccountId;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Tranfer amount must be greater than zero")
    private Long amount;

    public Long getTargetAccountId() {
        return targetAccountId;
    }

    public Long getAmount() {
        return amount;
    }
}
