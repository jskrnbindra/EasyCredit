package com.easycredit.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private final String sessionId;
    private final EasyCreditUser user;

    public LoggedInUser(EasyCreditUser user, String sessionId) {
        this.user = user;
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public EasyCreditUser getUser() {
        return user;
    }
}