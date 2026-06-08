package com.example.chatproject;

public class Message {
    private int id;
    private int fromUserId;
    private int toUserId;
    private String content;
    private String timestamp;
    private boolean isRead; // <-- NEW FIELD

    // UPDATED CONSTRUCTOR
    public Message(int id, int fromUserId, int toUserId, String content, String timestamp, boolean isRead) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    // getters
    public int getId() { return id; }
    public int getFromUserId() { return fromUserId; }
    public int getToUserId() { return toUserId; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; } // <-- NEW GETTER
}