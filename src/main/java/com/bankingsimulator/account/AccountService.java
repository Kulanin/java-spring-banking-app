
package com.bankingsimulator.account;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bankingsimulator.account.dto.TransactionRequestDto;
import com.bankingsimulator.audit.AuditService;
import com.bankingsimulator.outbox.OutboxService;
import com.bankingsimulator.pdf.PdfGenerationService;
import com.bankingsimulator.rabbitmq.RabbitMQConfig;
import com.bankingsimulator.transaction.TransactionMapper;
import com.bankingsimulator.transaction.TransactionRecord;
import com.bankingsimulator.transaction.TransactionRecordService;
import com.bankingsimulator.transaction.TransactionType;
import com.bankingsimulator.transaction.dto.TransactionResponseDto;
import com.bankingsimulator.user.User;
import com.bankingsimulator.user.UserRepository;

@Slf4j
@Service
public class AccountService {

    final private UserRepository userRepository;

    final private AccountRepository accountRepository;

    final private TransactionRecordService transactionRecordService;

    final private AccountFactory accountFactory;

    final private AuditService auditService;
    final private TransactionMapper transactionMapper;
    final private PdfGenerationService pdfGenerationService;
    private final OutboxService outboxService;

    public AccountService(UserRepository userRepository, AccountRepository accountRepository,
            TransactionRecordService transactionRecordService, AccountFactory accountFactory,
            AuditService auditService, TransactionMapper transactionMapper, PdfGenerationService pdfGenerationService,
            OutboxService outboxService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRecordService = transactionRecordService;
        this.accountFactory = accountFactory;
        this.auditService = auditService;
        this.transactionMapper = transactionMapper;
        this.pdfGenerationService = pdfGenerationService;
        this.outboxService = outboxService;

    }

    @Transactional
    public Account createAccountForUser(String idempotencyKey, Long userId, AccountType accountType,
            LocalDate maturityDate,
            String accountName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (accountRepository.existsByUserIdAndAccountName(userId, accountName)) {
            throw new IllegalArgumentException("An account with the name '" + accountName + "' already exists.");
        }

        Account account = accountFactory.createAccount(accountType, maturityDate);

        account.setBalance(0L);
        account.setAccountType(accountType);
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountName(accountName);

        user.addAccount(account);

        userRepository.save(user);
        auditService.logAction(idempotencyKey, "test-user-c", "CREATE-ACCOUNT",
                "Account name : " + accountName + " created successfullly");

        return account;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Account getAccountForUpdate(Long accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Transactional(readOnly = true)
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Transactional
    public TransactionResponseDto deposit(Long accountId, long amount, String idempotencyKey) {

        Optional<TransactionRecord> existingTransaction = transactionRecordService.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            TransactionRecord existing = existingTransaction.get();
            return transactionMapper.toDuplicateResponse(existing);
        }
        Account account = getAccountForUpdate(accountId);
        account.deposit(amount);

        TransactionRecord record = transactionMapper.toEntity(
                new TransactionRequestDto(amount),
                account,
                idempotencyKey,
                TransactionType.DEPOSIT);

        transactionRecordService.save(record);

        outboxService.stageTransactionEvent(accountId, amount, TransactionType.DEPOSIT, idempotencyKey, record.getId(),
                RabbitMQConfig.TX_DEPOSIT_KEY);

        return transactionMapper.toResponseDto(record, "Cash deposited successfully");
    }

    @Transactional
    public TransactionResponseDto withdraw(Long accountId, long amount, String idempotencyKey) {

        Optional<TransactionRecord> existingTransaction = transactionRecordService.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            TransactionRecord existing = existingTransaction.get();
            return transactionMapper.toDuplicateResponse(existing);
        }
        Account account = getAccountForUpdate(accountId);

        account.withdraw(amount);

        TransactionRecord record = transactionMapper.toEntity(
                new TransactionRequestDto(amount),
                account,
                idempotencyKey,
                TransactionType.WITHDRAW);

        transactionRecordService.save(record);
        outboxService.stageTransactionEvent(accountId, amount, TransactionType.WITHDRAW, idempotencyKey, record.getId(),
                RabbitMQConfig.TX_WITHDRAWAL_KEY);

        return transactionMapper.toResponseDto(record, "Cash withdrawal successful");
    }

    public Map<String, Object> generateStatement(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        CompletableFuture<String> future = this.pdfGenerationService.generateStatement(account);

        future.thenAccept(fileUrl -> {

            log.info("Async PDF completed! URL saved: {}", fileUrl);
            // notificationService.sendEmail(account.getEmail(), fileUrl);
        }).exceptionally(ex -> {
            log.error("Async PDF failed for account id {}: {}", accountId, ex.getMessage());
            // notificationService.sendFailureAlert(account.getUserId());
            return null;
        });
        return Map.of(
                "status", "processing",
                "message", "PDF generation started. You will be notified when complete.");

    }

}
