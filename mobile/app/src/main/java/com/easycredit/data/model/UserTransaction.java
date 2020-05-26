package com.easycredit.data.model;

import com.easycredit.data.enums.TransactionStatus;

import java.util.Date;

/**
 * A projection of {@link Transaction} which helps
 * in quickly populating UI.
 */
public class UserTransaction {

    private String beneficiaryId;
    private String transactionId;

    private String beneficiaryName;
    private int amount;
    private Date timestamp;
    private TransactionStatus status;

    public UserTransaction(String beneficiaryName, String beneficiaryId,
                       int amount, Date timestamp, TransactionStatus status,
                       String transactionId) {
        this.beneficiaryId = beneficiaryId;
        this.transactionId = transactionId;

        this.beneficiaryName = beneficiaryName;
        this.amount = amount;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
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
