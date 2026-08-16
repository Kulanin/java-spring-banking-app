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

import com.demo.account.dto.AccountCreationRequestDto;
import com.demo.account.dto.AccountResponseDto;
import com.demo.account.dto.TransactionRequestDto;
import com.demo.common.ApiResponse;
import com.demo.transaction.TransactionRecordService;
import com.demo.transaction.dto.TransactionResponseDto;
import com.demo.transaction.dto.TransactionStatementDto;
import com.demo.transaction.dto.TransactionStatus;
import com.demo.transaction.dto.TransferRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/v1/accounts")
@Validated
public class AccountController {

    private final AccountService accountService;
    private final TransferService transferService;
    private final TransactionRecordService transactionRecordService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, TransferService transferService,
            TransactionRecordService transactionRecordService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.transferService = transferService;
        this.transactionRecordService = transactionRecordService;
        this.accountMapper = accountMapper;
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> deposit(
            @PathVariable @Positive Long accountId,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody TransactionRequestDto request) {
        TransactionResponseDto response = accountService.deposit(accountId, request.getAmount(), idempotencyKey);

        if (TransactionStatus.ALREADY_PROCESSED.equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.success(response, "Transaction already processed"));
        }
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> withdraw(
            @PathVariable Long accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody TransactionRequestDto request) {

        TransactionResponseDto response = accountService.withdraw(accountId, request.getAmount(), idempotencyKey);

        if (TransactionStatus.ALREADY_PROCESSED.equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.success(response, response.getMessage()));
        }
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));

    }

    @PostMapping("/{accountId}/transfer")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> transfer(
            @PathVariable Long accountId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        TransactionResponseDto response = transferService.transfer(
                accountId, request.getTargetAccountId(), request.getAmount(), idempotencyKey);

        if (TransactionStatus.ALREADY_PROCESSED.equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.success(response));
        }
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Account>> createAccount(@PathVariable Long userId,
            @Valid @RequestBody AccountCreationRequestDto request) {
        Account newAccount = accountService.createAccountForUser(userId, request.getAccountType(),
                request.getMaturityDate(), request.getAccountName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(newAccount, "Account created successfully"));

    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> geAccount(@PathVariable Long accountId) {
        Account account = accountService.getAccount(accountId);
        AccountResponseDto accountResponseDto = accountMapper.toResponseDto(account);
        return ResponseEntity.ok(ApiResponse.success(accountResponseDto, "Account retrieved successully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getAllAccounts() {
        List<AccountResponseDto> responseDtos = accountService.findAll().stream().map(accountMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responseDtos, "Accounts retrieved successfully"));
    }

    @GetMapping("/statement/{accountId}")
    public ResponseEntity<ApiResponse<List<TransactionStatementDto>>> getStatement(@PathVariable Long accountId) {
        List<TransactionStatementDto> statementDtos = transactionRecordService.getStatement(accountId);
        return ResponseEntity.ok(ApiResponse.success(statementDtos, "Statement retrieved successfully"));
    }

}
