package com.demo.account.dto;

import java.time.LocalDate;

import com.demo.account.AccountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationRequestDto {
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    private LocalDate maturityDate;
    @NotBlank(message = "Account name is required")
    private String accountName;

}
