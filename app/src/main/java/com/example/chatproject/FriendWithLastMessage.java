package com.example.chatproject;

public class FriendWithLastMessage {
    User friend;
    LastMessageInfo lastMessageInfo;

    FriendWithLastMessage(User friend, LastMessageInfo lastMessageInfo) {
        this.friend = friend;
        this.lastMessageInfo = lastMessageInfo;
    }
}
