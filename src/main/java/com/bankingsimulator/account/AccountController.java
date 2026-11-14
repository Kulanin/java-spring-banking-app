package com.bankingsimulator.account;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bankingsimulator.account.dto.AccountCreationRequestDto;
import com.bankingsimulator.account.dto.AccountResponseDto;
import com.bankingsimulator.account.dto.TransactionRequestDto;
import com.bankingsimulator.common.ApiResponse;
import com.bankingsimulator.common.PageRequestDto;
import com.bankingsimulator.common.PageResponseDto;
import com.bankingsimulator.transaction.TransactionRecordService;
import com.bankingsimulator.transaction.TransactionType;
import com.bankingsimulator.transaction.dto.TransactionResponseDto;
import com.bankingsimulator.transaction.dto.TransactionStatementDto;
import com.bankingsimulator.transaction.dto.TransactionStatus;
import com.bankingsimulator.transaction.dto.TransferRequest;

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
    private static final Set<String> ALLOWED_SORTS = Set.of("id", "createdAt", "amount", "type");

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
            @PathVariable @Positive Long accountId,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
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
            @PathVariable @Positive Long accountId,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        TransactionResponseDto response = transferService.transfer(
                accountId, request.getTargetAccountId(), request.getAmount(), idempotencyKey);

        if (TransactionStatus.ALREADY_PROCESSED.equals(response.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.success(response));
        }
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> createAccount(@PathVariable Long userId,
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody AccountCreationRequestDto request) {
        Account newAccount = accountService.createAccountForUser(idempotencyKey, userId, request.getAccountType(),
                request.getMaturityDate(), request.getAccountName());
        AccountResponseDto accountResponseDto = accountMapper.toResponseDto(newAccount);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(accountResponseDto, "Account created successfully"));

    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> getAccount(@PathVariable Long accountId) {
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
    public ResponseEntity<ApiResponse<PageResponseDto<TransactionStatementDto>>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Valid PageRequestDto pageRequest) {
        Page<TransactionStatementDto> transactions = transactionRecordService.getTransactions(
                accountId,
                type,
                startDate,
                endDate,
                pageRequest.toPageable(ALLOWED_SORTS));

        return ResponseEntity.ok(
                ApiResponse.success(PageResponseDto.from(transactions), "Statement retrieved successfully"));
    }

    @GetMapping("/download/{accountId}")
    public ResponseEntity<Map<String, Object>> downloadStatement(@PathVariable Long accountId) {
        // Not production ready yet and i will review it
        Map<String, Object> response = this.accountService.generateStatement(accountId);

        return ResponseEntity.accepted().body(response);

    }

    // @GetMapping("/jobs/{jobId}")
    // public ResponseEntity<JobStatusDto> jobStatus(@PathVariable UUID jobId) {
    // return ResponseEntity.ok(accountService.getStatus(jobId));
    // }

}
