package com.demo.account;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.demo.account.dto.AccountResponseDto;
import com.demo.account.dto.AccountSummaryDto;

@Component
public class AccountMapper {

    public AccountSummaryDto toSummaryDto(Account account) {
        if (account == null) {
            return null;
        }

        return AccountSummaryDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .build();
    }

    public List<AccountSummaryDto> toSummaryDtoList(List<Account> accounts) {
        if (accounts == null) {
            return Collections.emptyList();
        }

        return accounts.stream()
                .map(this::toSummaryDto)
                .toList();
    }

    public AccountResponseDto toResponseDto(Account account) {
        if (account == null) {
            return null;
        }

        AccountResponseDto.AccountResponseDtoBuilder builder = AccountResponseDto.builder()
                .id(account.getId())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .accountName(account.getAccountName());

        // Handle type-specific fields
        if (account instanceof FixedAccount fixedAccount) {
            builder.maturityDate(fixedAccount.getMaturityDate());
        }

        return builder.build();
    }

}
