package com.example.healthtracking.model;

public class Message {
    private String senderId;       // Mesaj gönderenin userId'si
    private String receiverId;    // Mesaj alanın userId'si
    private String messageText;   // Mesaj metni
    private String senderRole;    // Gönderenin rolü (USER/DOCTOR)
    private boolean isRead;       // Mesaj okunmuş mu?

    public Message() {}

    public Message(String senderId, String receiverId, String messageText, String senderRole, boolean isRead) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.senderRole = senderRole;
        this.isRead = isRead;
    }

    // Getters and Setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}
