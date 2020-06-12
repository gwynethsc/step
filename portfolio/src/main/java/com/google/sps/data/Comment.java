package com.google.sps.data;

/** A visitor comment for a website */

public final class Comment {
    
    // constants used for Datastore operations
    public static final String KIND = "Comment";
    public static final String PROPERTY_TIMESTAMP = "timestamp";
    public static final String PROPERTY_ID = "userId";
    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_TEXT = "text";
    public static final String PROPERTY_IMAGE = "image";

    private final String key;
    private final long timestamp;
    private final String userId;
    private final String userEmail;
    private final String text;

    public Comment(String key, long timestamp, String userId, String userEmail, String text) {
        this.key = key;
        this.timestamp = timestamp;
        this.userId = userId;
        this.userEmail = userEmail;
        this.text = text;
    }
}
