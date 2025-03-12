package com.example.examplefirst;

public class Contact {
    private String name;
    private String waId;
    private String lastMessageTime;
    private int messageCount; // Add this field

    public Contact(String name, String waId, String lastMessageTime, int messageCount) {
        this.name = name;
        this.waId = waId;
        this.lastMessageTime = lastMessageTime;
        this.messageCount = messageCount;
    }
    public int getMessageCount() {
        return messageCount;
    }

    public String getName() {
        return name;
    }

    public String getWaId() {
        return waId;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }
}
