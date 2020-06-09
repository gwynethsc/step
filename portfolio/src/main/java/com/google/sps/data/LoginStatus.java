package com.google.sps.data;

/** A current login status for the website */

public final class LoginStatus {
    
    private final boolean loggedIn;
    private final String user;
    private final String logURL; // login if !loggedIn, else logout URL

    public LoginStatus(boolean loggedIn, String user, String logURL) {
        this.loggedIn = loggedIn;
        this.user = user;
        this.logURL = logURL;
    }
}
