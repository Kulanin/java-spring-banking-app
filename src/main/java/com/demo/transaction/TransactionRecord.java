package com.demo.transaction;

import java.time.LocalDateTime;

import com.demo.account.Account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    // Only map the relationship object
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private long amount;
    private long balanceAfter;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private LocalDateTime createdAt;

    @Column(name = "account_name")
    private String accountName;

    public TransactionRecord() {
    }

    public TransactionRecord(String idempotencyKey, Account account, long amount, TransactionType type,
            long balanceAfter, String accountName) {
        this.idempotencyKey = idempotencyKey;
        this.account = account;
        this.amount = amount;
        this.type = type;
        this.balanceAfter = balanceAfter;
        this.accountName = accountName;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    // Safely pull the ID from the Account object
    public Long getAccountId() {
        return account != null ? account.getId() : null;
    }

    public Account getAccount() {
        return account;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public long getBalanceAfter() {
        return balanceAfter;
    }

    public String getAccountName() {
        return accountName != null ? accountName : (account != null ? account.getAccountName() : null);
    }
}