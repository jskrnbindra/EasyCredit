package com.easycredit.data.model;

import com.easycredit.data.enums.TransactionStatus;

import java.util.Date;

/**
 * A transaction an {@link EasyCreditUser} might do.
 */
public class Transaction {
    private EasyCreditUser beneficiary;
    private int amount;
    private Date timestamp;
    private TransactionStatus status;
    private String transactionId;

    public Transaction(EasyCreditUser beneficiary, int amount, Date timestamp,
                       TransactionStatus status, String transactionId) {
        this.beneficiary = beneficiary;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
        this.transactionId = transactionId;
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

    public String getTransactionId() {
        return transactionId;
    }
}
