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
     * The transaction has started and payment link
     * has been sent.
     */
    STARTED,

    /**
     * Payment link has been sent.
     */
    LINK_SENT,

    /**
     * Payment has been received from payment link.
     */
    MIDWAY,

    /**
     * Cashgram sent but not redeemed yet.
     */
    PENDING;
}
