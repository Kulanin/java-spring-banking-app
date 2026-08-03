package com.demo.account.dto;

import java.time.LocalDate;

import com.demo.account.AccountType;

public class AccountResponseDto {

    private Long id;
    private Long balance;
    private AccountType accountType;
    private LocalDate maturityDate;
    private String accountName;

    public AccountResponseDto(Long id, Long balance, AccountType accountType, LocalDate maturityDate,
            String accountName) {
        this.id = id;
        this.balance = balance;
        this.accountType = accountType;
        this.maturityDate = maturityDate;
        this.accountName = accountName;

    }

    // static factory method to map from the entity cleanly
    public static AccountResponseDto fromEntity(com.demo.account.Account account) {
        LocalDate maturity = null;

        if (account instanceof com.demo.account.FixedAccount fixedAccount) {
            maturity = fixedAccount.getMaturityDate();
        }

        return new AccountResponseDto(
                account.getId(),
                account.getBalance(),
                account.getAccountType(),
                maturity,
                account.getAccountName());
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getBalance() {
        return balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public LocalDate LocalDate() {
        return maturityDate;
    }

    public String getAccountName() {
        return accountName;
    }

}
