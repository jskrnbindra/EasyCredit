package com.easycredit.data.model;

import java.util.List;

/**
 * A user of EasyCredit
 */
public final class EasyCreditUser {

    private final String id;
    private final String email;
    private final String name;
    private final String phone;
    private final List<UserTransaction> transactions;

    public EasyCreditUser(String id, String email, String name, String phone, List<UserTransaction> transactions) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.transactions = transactions;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public List<UserTransaction> getTransactions() {
        return transactions;
    }
}
