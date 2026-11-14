package com.bankingsimulator.account.dto;

import com.bankingsimulator.account.AccountStatus;
import com.bankingsimulator.account.AccountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummaryDto {
    private Long id;
    private String accountNumber;
    private String accountName;
    private long balance;
    private AccountType accountType;
    private AccountStatus status;
}