package com.demo.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.demo.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.GenerationType;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor

public abstract class Account implements Depositable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String accountNumber;

    private long balance;
    private BigDecimal interestRate;
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", insertable = false, updatable = false)
    private AccountType accountType;

    private String accountName;

    private long balanceAfter;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Reference to user feature
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Override
    public void deposit(long amount) {
        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
    }

    void withdraw(long amount) {
        throw new UnsupportedOperationException("Withdrawals are not supported for this account type.");
    }

    // @Override
    public long getBalance() {
        return this.balance;
    }

    // Example generator method
    public String generateAccountNumber() {
        return "ACC-" + LocalDateTime.now().getYear() +
                "-" + String.format("%06d", (long) (Math.random() * 1000000));
    }

    public String getAccountName() {
        return accountName;
    }

    public void SetAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getBalancAfter() {
        return balanceAfter;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.accountNumber == null) {
            this.accountNumber = generateAccountNumber();
        }
    }

}
