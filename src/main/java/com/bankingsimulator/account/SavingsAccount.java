package com.bankingsimulator.account;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SAVINGS")
public class SavingsAccount extends Account {

}