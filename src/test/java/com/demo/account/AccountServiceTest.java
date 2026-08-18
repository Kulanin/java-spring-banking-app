package com.demo.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.demo.account.dto.TransactionRequestDto;
import com.demo.audit.AuditService;
import com.demo.transaction.TransactionMapper;
import com.demo.transaction.TransactionRecord;
import com.demo.transaction.TransactionRecordService;
import com.demo.transaction.TransactionType;
import com.demo.transaction.dto.TransactionResponseDto;
import com.demo.transaction.dto.TransactionStatus;
import com.demo.user.User;
import com.demo.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRecordService transactionRecordService;

    @Mock
    private AccountFactory accountFactory;

    @InjectMocks
    private AccountService accountService;
    @Mock
    private AuditService auditService;

    @Mock
    private TransactionMapper transactionMapper;

    @Test
    void createAccountForUser_Success() {
        // Arrange
        Long userId = 1L;
        String accountName = "Holiday Savings";
        AccountType type = AccountType.CHECKING;
        LocalDate maturityDate = null;

        User mockUser = new User();
        mockUser.setId(userId);

        Account mockAccount = new CheckingAccount();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(accountRepository.existsByUserIdAndAccountName(userId, accountName)).thenReturn(false);
        when(accountFactory.createAccount(type, maturityDate)).thenReturn(mockAccount);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        Account result = accountService.createAccountForUser(userId, type, maturityDate, accountName);

        // Assert
        assertNotNull(result);
        assertEquals(accountName, result.getAccountName());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(0L, result.getBalance());

        verify(userRepository, times(1)).findById(userId);
        verify(accountRepository, times(1)).existsByUserIdAndAccountName(userId, accountName);
        verify(accountFactory, times(1)).createAccount(type, maturityDate);
        verify(userRepository, times(1)).save(mockUser);

    }

    @Test
    void createAccountForUser_UserNotFound_ThrowsException() {

        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.createAccountForUser(userId, AccountType.CHECKING, null, null));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(accountFactory, accountRepository);
    }

    @Test
    void createAccountForUser_DuplicateName_ThrowsException() {

        Long userId = 1L;
        String accountName = "Main";
        User mockUser = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(accountRepository.existsByUserIdAndAccountName(userId, accountName)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccountForUser(userId, AccountType.CHECKING, null, accountName));

        assertTrue(exception.getMessage().contains("already exists"));
        verify(accountRepository, times(1)).existsByUserIdAndAccountName(userId, accountName);
        verifyNoInteractions(accountFactory);

    }

    @Test
    void deposit_Success() {
        Long accountId = 1L;
        long depositAmount = 500L;
        String idempotencyKey = "key-123";

        Account mockAccount = new CheckingAccount();
        mockAccount.setId(accountId);
        mockAccount.setBalance(1000L);
        mockAccount.setAccountType(AccountType.CHECKING);

        when(transactionRecordService.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        TransactionRecord mockRecord = new TransactionRecord();
        when(transactionMapper.toEntity(any(TransactionRequestDto.class), any(Account.class), eq(idempotencyKey),
                eq(TransactionType.DEPOSIT)))
                .thenReturn(mockRecord);

        TransactionResponseDto mockResponse = new TransactionResponseDto();
        mockResponse.setStatus(TransactionStatus.SUCCESS);
        mockResponse.setBalanceAfter(1500L);
        mockResponse.setMessage("Deposit successful");

        when(transactionMapper.toResponseDto(any(TransactionRecord.class), anyString()))
                .thenReturn(mockResponse);

        // Act
        TransactionResponseDto response = accountService.deposit(accountId, depositAmount, idempotencyKey);

        // Assert
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(1500L, response.getBalanceAfter());
        assertEquals("Deposit successful", response.getMessage());

        verify(transactionRecordService, times(1)).save(any(TransactionRecord.class));

    }

    @Test
    void deposit_AlreadyProcessed_ReturnsIdempotentResponse() {

        String idempotencyKey = "key-duplicate";
        TransactionRecord existingRecord = new TransactionRecord(null, idempotencyKey, null, 0, 0, null, null,
                idempotencyKey);

        when(transactionRecordService.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingRecord));

        TransactionResponseDto mockResponse = new TransactionResponseDto();
        mockResponse.setStatus(TransactionStatus.ALREADY_PROCESSED);
        mockResponse.setBalanceAfter(1500L);
        mockResponse.setMessage("This transaction was already completed");

        when(transactionMapper.toDuplicateResponse(any(TransactionRecord.class)))
                .thenReturn(mockResponse);

        // Act
        TransactionResponseDto response = accountService.deposit(1L, 500, idempotencyKey);

        // Assert
        assertEquals(TransactionStatus.ALREADY_PROCESSED, response.getStatus());
        assertEquals("This transaction was already completed", response.getMessage());

        // Ensure deposit never touches account repo or modifies balance
        verifyNoInteractions(accountRepository);
        verify(transactionRecordService, never()).save(any());
    }

    @Test
    void withdraw_Success() {
        // Arrange
        Long accountId = 1L;
        long withdrawAmount = 200L;
        String idempotencyKey = "key-withdraw";

        Account mockAccount = new CheckingAccount();
        mockAccount.setId(accountId);
        mockAccount.setBalance(1000L);
        mockAccount.setAccountType(AccountType.CHECKING);

        when(transactionRecordService.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        TransactionRecord mockRecord = new TransactionRecord();
        when(transactionMapper.toEntity(any(TransactionRequestDto.class), any(Account.class), eq(idempotencyKey),
                eq(TransactionType.WITHDRAW)))
                .thenReturn(mockRecord);

        TransactionResponseDto mockResponse = new TransactionResponseDto();
        mockResponse.setStatus(TransactionStatus.SUCCESS);
        mockResponse.setBalanceAfter(800L);
        mockResponse.setMessage("Withdrawal successful");

        when(transactionMapper.toResponseDto(any(TransactionRecord.class), anyString()))
                .thenReturn(mockResponse);

        // Act
        TransactionResponseDto response = accountService.withdraw(accountId, withdrawAmount, idempotencyKey);

        // Assert
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(800L, response.getBalanceAfter());
        assertEquals("Withdrawal successful", response.getMessage());

        verify(transactionRecordService, times(1)).save(any(TransactionRecord.class));

    }

}
