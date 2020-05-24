package com.easycredit.data.enums;

public enum TransactionStatus {

    /**
     * Transaction completed.
     */
    DONE,

    /**
     * The sent CashGram has expired and was
     * not redeemed.
     */

    EXPIRED,
    /**
     * Cashgram sent but not redeemed yet.
     */
    PENDING;
}
