package com.demo.account.dto;

import com.demo.account.AccountType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.demo.account.AccountStatus;

@Data
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