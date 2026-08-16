
package com.demo.account;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.demo.account.dto.TransactionRequestDto;
import com.demo.audit.AuditService;
import com.demo.transaction.TransactionMapper;
import com.demo.transaction.TransactionRecord;
import com.demo.transaction.TransactionRecordService;
import com.demo.transaction.TransactionType;
import com.demo.transaction.dto.TransactionResponseDto;
import com.demo.user.User;
import com.demo.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    final private UserRepository userRepository;

    final private AccountRepository accountRepository;

    final private TransactionRecordService transactionRecordService;

    final private AccountFactory accountFactory;

    final private AuditService auditService;
    final private TransactionMapper transactionMapper;

    public AccountService(UserRepository userRepository, AccountRepository accountRepository,
            TransactionRecordService transactionRecordService, AccountFactory accountFactory,
            AuditService auditService, TransactionMapper transactionMapper) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRecordService = transactionRecordService;
        this.accountFactory = accountFactory;
        this.auditService = auditService;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public Account createAccountForUser(Long userId, AccountType accountType, LocalDate maturityDate,
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
        auditService.logAction("test-user-c", "CREATE-ACCOUNT",
                "Account name : " + accountName + " created successfullly");

        return account;
    }

    public Account getAccount(Long accountNumber) {
        return accountRepository.findById(accountNumber).orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Transactional
    public TransactionResponseDto deposit(Long accountId, long amount, String idempotencyKey) {

        Optional<TransactionRecord> existingTransaction = transactionRecordService.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {

            TransactionRecord existing = existingTransaction.get();
            auditService.logAction("test-user-d", "DEPOSIT",
                    "Successfully deposited " + amount + " into account ID: " + accountId);
            return transactionMapper.toDuplicateResponse(existing);
        }
        Account account = getAccount(accountId); // Reuse your helper method
        account.deposit(amount);

        TransactionRecord record = transactionMapper.toEntity(
                new TransactionRequestDto(amount),
                account,
                idempotencyKey,
                TransactionType.DEPOSIT);

        transactionRecordService.save(record);
        auditService.logAction("test-user-d", "DEPOSIT",
                "Successfully deposited " + amount + " into account ID: " + accountId);

        return transactionMapper.toResponseDto(record, "Cash deposited successfully");
    }

    @Transactional
    public TransactionResponseDto withdraw(Long accountId, long amount, String idempotencyKey) {

        Optional<TransactionRecord> existingTransaction = transactionRecordService.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {

            TransactionRecord existing = existingTransaction.get();
            auditService.logAction(
                    "test-user-w",
                    "WITHDRAWAL",
                    "Successfully withdrew " + amount + " from account ID: " + accountId);
            return transactionMapper.toDuplicateResponse(existing);

        }
        Account account = getAccount(accountId);

        account.withdraw(amount);

        TransactionRecord record = transactionMapper.toEntity(
                new TransactionRequestDto(amount),
                account,
                idempotencyKey,
                TransactionType.WITHDRAW);

        transactionRecordService.save(record);

        auditService.logAction(
                "test-user-w",
                "WITHDRAWAL",
                "Successfully withdrew " + amount + " from account ID: " + accountId);
        return transactionMapper.toResponseDto(record, "Cash withdrawal successful");
    }

}
