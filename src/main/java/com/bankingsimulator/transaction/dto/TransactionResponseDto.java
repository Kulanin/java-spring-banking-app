
package com.bankingsimulator.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.bankingsimulator.transaction.TransactionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    private Long id;
    private Long accountId;
    private Long amount;
    private TransactionType type;
    private TransactionStatus status;
    private Long balanceAfter;
    private String message;
    private String transactionReference;
    private LocalDateTime timestamp;

}