package com.google.sps.data;

/** A current login status for the website */

public final class LoginStatus {
    
    private final boolean loggedIn;
    private final String userId;
    private final String userEmail;
    private final String logURL; // login if !loggedIn, else logout URL

    public LoginStatus(boolean loggedIn, String userId, String userEmail, String logURL) {
        this.loggedIn = loggedIn;
        this.userId = userId;
        this.userEmail = userEmail;
        this.logURL = logURL;
    }
}
