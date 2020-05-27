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
     * The transaction has just started and payment link
     * sending is in process.
     */
    STARTED,

    /**
     * Payment link has been sent.
     */
    LINK_SENT,

    /**
     * Payment has been received.
     */
    PAYMENT_RECEIVED,

    /**
     * Cashgram sent but not redeemed yet.
     */
    PENDING;
}
