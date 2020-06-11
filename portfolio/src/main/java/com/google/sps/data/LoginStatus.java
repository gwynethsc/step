package com.google.sps.data;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/** A current login status for the website */

public final class LoginStatus {
    
    private static final UserService userService = UserServiceFactory.getUserService();

    private final boolean loggedIn;
    private final String userId;
    private final String userEmail;
    private final String authenticationURL; // points to login page if !loggedIn, else logout URL

    /**
     * Constructs a new LoginStatus object
     *
     * @param loggedIn      true if a user is logged in, else false
     * @param userId        a unique String identifying the user, if logged in; else null
     * @param userEmail     
     * @param redirectURL   the URL to be redirected to after logging in or out
     */
    public LoginStatus(boolean loggedIn, String userId, String userEmail, String redirectURL) {
        this.loggedIn = loggedIn;
        this.userId = userId;
        this.userEmail = userEmail;
        if (this.loggedIn) {
            this.authenticationURL = userService.createLogoutURL(redirectURL);
        } else {
            this.authenticationURL = userService.createLoginURL(redirectURL);
        }
    }
}
