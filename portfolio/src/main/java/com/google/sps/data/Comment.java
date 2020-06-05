package com.google.sps.data;

/** A visitor comment for a website */

public final class Comment {
    private final String key;
    private final long timestamp;
    private final String text;

    public Comment (String key, long timestamp, String text) {
        this.key = key;
        this.timestamp = timestamp;
        this.text = text;
    }
}
