package com.demo.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.demo.exceptions.InsufficientFundsException;
import com.demo.exceptions.InvalidTransactionAmountException;

public class CheckingAccountTest {

    @Test
    void withdraw_Success() {
        CheckingAccount account = new CheckingAccount();
        account.setBalance(1000L);

        account.withdraw(300L);

        assertEquals(700L, account.getBalance());
    }

    @Test
    void withdraw_ZeroOrNegativeAmount_ThrowsException() {
        CheckingAccount account = new CheckingAccount();
        account.setBalance(1000L);

        assertThrows(InvalidTransactionAmountException.class, () -> {
            account.withdraw(-50L);
        });

    }

    @Test
    void withdraw_InsufficientFunds_ThrowsException() {
        CheckingAccount account = new CheckingAccount();
        account.setBalance(500L);

        assertThrows(InsufficientFundsException.class, () -> {
            account.withdraw(600L);
        });
    }

}
