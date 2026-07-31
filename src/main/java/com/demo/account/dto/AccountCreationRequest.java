package com.demo.account.dto;

import java.time.LocalDate;

import com.demo.account.AccountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AccountCreationRequest {
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    private LocalDate maturityDate;

    @NotBlank(message = "Account name is required")
    private String accountName;

    public AccountType getAccountType() {
        return accountType;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public String getAccountName() {
        return accountName;
    }
}
