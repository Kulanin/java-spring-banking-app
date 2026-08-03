package com.demo.account;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.account.dto.AccountCreationRequest;
import com.demo.account.dto.AccountResponseDto;
import com.demo.account.dto.TransactionRequest;
import com.demo.common.ApiResponse;
import com.demo.transaction.TransactionRecordService;
import com.demo.transaction.dto.TransactionResponse;
import com.demo.transaction.dto.TransactionStatementDto;
import com.demo.transaction.dto.TransactionStatus;
import com.demo.transaction.dto.TransferRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {

    private final AccountService accountService;
    private final TransferService transferService;
    private final TransactionRecordService transactionRecordService;

    public AccountController(AccountService accountService, TransferService transferService,
            TransactionRecordService transactionRecordService) {
        this.accountService = accountService;
        this.transferService = transferService;
        this.transactionRecordService = transactionRecordService;
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable @Positive Long accountId,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey, // Access the header
            @Valid @RequestBody TransactionRequest request // Access the JSON body
    ) {
        TransactionResponse response = accountService.deposit(accountId, request.getAmount(), idempotencyKey);

        if (TransactionStatus.ALREADY_PROCESSED.equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody TransactionRequest request) {

        TransactionResponse response = accountService.withdraw(accountId, request.getAmount(), idempotencyKey);

        if (TransactionStatus.ALREADY_PROCESSED.equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.ok(response);

    }

    @PostMapping("/{accountId}/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable Long accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        TransactionResponse response = transferService.transfer(
                accountId, request.getTargetAccountId(), request.getAmount(), idempotencyKey);

        if (TransactionStatus.ALREADY_PROCESSED.equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Account>> createAccount(@PathVariable Long userId,
            @Valid @RequestBody AccountCreationRequest request) {
        Account newAccount = accountService.createAccountForUser(userId, request.getAccountType(),
                request.getMaturityDate(), request.getAccountName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newAccount, "Account created successfully"));

    }

    @GetMapping("/{accountId}")
    public AccountResponseDto geAccount(@PathVariable Long accountId) {
        Account account = accountService.getAccount(accountId);
        return AccountResponseDto.fromEntity(account);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
        List<AccountResponseDto> responseDtos = accountService.findAll().stream().map(AccountResponseDto::fromEntity)
                .toList();
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/statement/{accountId}")
    public ResponseEntity<List<TransactionStatementDto>> getStatement(@PathVariable Long accountId) {
        List<TransactionStatementDto> statementDtos = transactionRecordService.getStatement(accountId);
        return ResponseEntity.ok(statementDtos);
    }

}
