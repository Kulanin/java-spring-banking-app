package com.demo.transaction.dto;

import com.demo.transaction.TransactionType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatementDto {
    private Long accountId;
    private long amount;
    private long balanceAfter;
    private LocalDateTime createdAt;
    private TransactionType type;
    private String accountName;
}