package me.pulsi_.bankplus.bankTop;

import java.math.BigDecimal;

public class BankTopPlayer {

    private BigDecimal balance;
    private String name;

    public BigDecimal getBalance() {
        return balance;
    }

    public String getName() {
        return name;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setName(String name) {
            this.name = name;
        }
}