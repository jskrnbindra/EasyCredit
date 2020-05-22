package com.easycredit.data.model;

/**
 * A user of EasyCredit
 */
public final class EasyCreditUser {

    private final String userId;
    private final String email;
    private final String displayName;
    private final String phone;

    public EasyCreditUser(String userId, String email, String displayName, String phone) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
