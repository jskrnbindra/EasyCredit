package com.easycredit.data.model;

import java.util.Date;

/**
 * A transaction an {@link EasyCreditUser} might do.
 */
public class UserTransaction {
    private EasyCreditUser beneficiary;
    private int amount;
    private Date timestamp;

    public UserTransaction(EasyCreditUser beneficiary, int amount, Date timestamp) {
        this.beneficiary = beneficiary;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public EasyCreditUser getBeneficiary() {
        return beneficiary;
    }

    public int getAmount() {
        return amount;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
