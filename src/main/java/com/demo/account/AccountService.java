
package com.demo.account;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import com.demo.audit.AuditService;
import com.demo.transaction.TransactionRecord;
import com.demo.transaction.TransactionRecordService;
import com.demo.transaction.TransactionType;
import com.demo.transaction.dto.TransactionResponse;
import com.demo.transaction.dto.TransactionStatus;
import com.demo.user.User;
import com.demo.user.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AccountService {

    final private UserRepository userRepository;

    final private AccountRepository accountRepository;

    final private TransactionRecordService transactionRecordService;

    final private AccountFactory accountFactory;

    final private AuditService auditService;

    public AccountService(UserRepository userRepository, AccountRepository accountRepository,
            TransactionRecordService transactionRecordService, AccountFactory accountFactory,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRecordService = transactionRecordService;
        this.accountFactory = accountFactory;
        this.auditService = auditService;
    }

    @Transactional
    public Account createAccountForUser(Long userId, AccountType accountType, LocalDate maturityDate,
            String accountName) {
        // 1. Fetch the user from the database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 2. Validate that the account name is unique for this user
        if (accountRepository.existsByUserIdAndAccountName(userId, accountName)) {
            throw new IllegalArgumentException("An account with the name '" + accountName + "' already exists.");
        }

        Account account = accountFactory.createAccount(accountType, maturityDate);

        account.setBalance(0L);
        account.setAccountType(accountType);
        account.setStatus(AccountStatus.ACTIVE);
        account.SetAccountName(accountName);

        // 3. Establish the relationship (using the helper method we discussed)
        user.addAccount(account);

        // 4. Save (CascadeType.ALL in User will automatically save the account)
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
    public TransactionResponse deposit(Long accountId, long amount, String idempotencyKey) {
        if (transactionRecordService.findByIdempotencyKey(idempotencyKey).isPresent()) {

            return new TransactionResponse(TransactionStatus.ALREADY_PROCESSED, 0,
                    "This transaction was already completed");
        }
        Account account = getAccount(accountId); // Reuse your helper method
        account.deposit(amount);
        long balanceAfter = account.getBalance();

        TransactionRecord record = new TransactionRecord(
                idempotencyKey, account, amount, TransactionType.DEPOSIT, balanceAfter, account.getAccountName());

        transactionRecordService.save(record);
        auditService.logAction("test-user-d", "DEPOSIT",
                "Successfully deposited " + amount + " into account ID: " + accountId);

        return new TransactionResponse(TransactionStatus.SUCCESS, account.getBalance(), "Deposit successful");
    }

    @Transactional
    public TransactionResponse withdraw(Long accountId, long amount, String idempotencyKey) {

        if (transactionRecordService.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return new TransactionResponse(TransactionStatus.ALREADY_PROCESSED, 0,
                    "This transaction was already completed");
        }
        Account account = getAccount(accountId);

        account.withdraw(amount);

        long balanceAfter = account.getBalance();

        TransactionRecord record = new TransactionRecord(
                idempotencyKey, account, amount, TransactionType.WITHDRAW, balanceAfter, account.getAccountName());
        transactionRecordService.save(record);

        auditService.logAction(
                "test-user-w",
                "WITHDRAWAL",
                "Successfully withdrew " + amount + " from account ID: " + accountId);
        return new TransactionResponse(TransactionStatus.SUCCESS, account.getBalance(), "Withdrawal successful");
    }

}
