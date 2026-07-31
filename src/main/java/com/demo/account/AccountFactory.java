package com.demo.account;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class AccountFactory {

    public Account createAccount(AccountType accountType, LocalDate maturityDate) {
        return switch (accountType) {
            case CHECKING -> new CheckingAccount();
            case FIXED -> {
                FixedAccount fixedAccount = new FixedAccount();
                if (maturityDate != null) {
                    fixedAccount.setMaturityDate(maturityDate);
                } else {
                    fixedAccount.setMaturityDate(LocalDate.now().plusMonths(3));
                }

                yield fixedAccount;
            }
            case SAVINGS -> new SavingsAccount();
            default -> throw new IllegalArgumentException("Unsupported account type: " + accountType);
        };

    }

}
