package com.demo.transaction;

import org.springframework.stereotype.Component;

import com.demo.account.Account;
import com.demo.account.dto.TransactionRequestDto;
import com.demo.transaction.dto.TransactionResponseDto;
import com.demo.transaction.dto.TransactionStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private long calculateBalanceAfter(Account account, long amount, TransactionType type) {
        return switch (type) {
            case DEPOSIT -> account.getBalance();
            case WITHDRAW -> account.getBalance();
            case TRANSFER_OUT -> account.getBalance();
            default -> account.getBalance();
        };
    }

    public TransactionRecord toEntity(TransactionRequestDto request,
            Account account,
            String idempotencyKey,
            TransactionType type) {
        if (request == null) {
            return null;
        }

        return TransactionRecord.builder()
                .idempotencyKey(idempotencyKey)
                .account(account)
                .amount(request.getAmount())
                .type(type)
                .balanceAfter(calculateBalanceAfter(account, request.getAmount(), type))
                .accountName(account.getAccountName())

                // .status(TransactionStatus.PENDING)
                // .transactionReference(generateReference())
                .build();
    }

    public TransactionResponseDto toResponseDto(TransactionRecord transaction, String message) {

        if (transaction == null) {
            return null;
        }

        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .balanceAfter(transaction.getBalanceAfter())
                .timestamp(transaction.getCreatedAt())
                .message(message)
                .build();
    }

    public TransactionResponseDto toDuplicateResponse(TransactionRecord existing) {
        if (existing == null) {
            return null;
        }

        return TransactionResponseDto.builder()
                .id(existing.getId())
                .accountId(existing.getAccount().getId())
                .amount(existing.getAmount())
                .type(existing.getType())
                .status(TransactionStatus.ALREADY_PROCESSED)
                .balanceAfter(existing.getBalanceAfter())
                .message("Transaction already processed")
                .build();
    }

}
