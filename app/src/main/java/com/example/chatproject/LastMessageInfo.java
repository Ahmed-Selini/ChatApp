package com.example.chatproject;

public class LastMessageInfo {
    private String content;
    private String timestamp;
    private int unreadCount;
    private boolean isLastSentMessageRead; // <-- NEW FIELD

    // NEW MAIN CONSTRUCTOR
    public LastMessageInfo(String content, String timestamp, int unreadCount, boolean isLastSentMessageRead) {
        this.content = content;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
        this.isLastSentMessageRead = isLastSentMessageRead;
    }

    // Existing constructor (for compatibility, although not used by DB now)
    public LastMessageInfo(String content, String timestamp, int unreadCount) {
        this(content, timestamp, unreadCount, false);
    }

    // Existing simplified constructor (for compatibility)
    public LastMessageInfo(String content, String timestamp) {
        this(content, timestamp, 0, false);
    }

    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getUnreadCount() { return unreadCount; }

    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public boolean isLastSentMessageRead() { return isLastSentMessageRead; } // <-- NEW GETTER
    public void setLastSentMessageRead(boolean lastSentMessageRead) { this.isLastSentMessageRead = lastSentMessageRead; } // <-- NEW SETTER
}