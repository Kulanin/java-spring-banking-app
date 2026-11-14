package com.bankingsimulator.account.dto;

import java.time.LocalDate;

import com.bankingsimulator.account.AccountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDto {

    private Long id;
    private Long balance;
    private AccountType accountType;
    private LocalDate maturityDate;
    private String accountName;

}
