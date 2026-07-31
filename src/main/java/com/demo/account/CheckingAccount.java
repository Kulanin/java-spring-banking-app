package com.demo.account;

import com.demo.exceptions.InsufficientFundsException;
import com.demo.exceptions.InvalidTransactionAmountException;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CHECKING")
public class CheckingAccount extends Account implements Withdrawable {

    @Override
    public void withdraw(long amount) {

        final long balance = getBalance();

        if (amount <= 0) {
            throw new InvalidTransactionAmountException("Withdrawal amount must be greater than zero.");
        }

        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal.");
        }

        setBalance(balance - amount);
    }

}
