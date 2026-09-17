package com.demo.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.demo.exceptions.InvalidAmountException;
import com.demo.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.GenerationType;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "accounts")
@NoArgsConstructor

public abstract class Account implements Depositable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Override
    public void deposit(long amount) {
        if (amount <= 0)
            throw new InvalidAmountException("Invalid amount :" + amount);
        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
    }

    void withdraw(long amount) {
        throw new UnsupportedOperationException("Withdrawals are not supported for this account type.");
    }

    public String generateAccountNumber() {
        return "ACC-" + LocalDateTime.now().getYear() +
                "-" + String.format("%06d", (long) (Math.random() * 1000000));
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.accountNumber == null) {
            this.accountNumber = generateAccountNumber();
        }
    }

}
