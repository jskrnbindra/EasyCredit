package com.easycredit.data.model;

import com.easycredit.data.enums.TransactionStatus;

import java.util.Date;

/**
 * A transaction an {@link EasyCreditUser} might do.
 */
public class UserTransaction {
    private EasyCreditUser beneficiary;
    private int amount;
    private Date timestamp;
    private TransactionStatus status;

    public UserTransaction(EasyCreditUser beneficiary, int amount, Date timestamp,
                           TransactionStatus status) {
        this.beneficiary = beneficiary;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
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

    public TransactionStatus getStatus() {
        return status;
    }
}
