package com.example.examplefirst;

import android.util.Log;

public class Contact {
    private String name;
    private String waId;
    private String lastMessageTime;
    private int messageCount;
    private boolean isActive;
    private String userName;


    public Contact(String name, String waId, String lastMessageTime, int messageCount,boolean isActive,String userName){
        this.name = name;
        this.waId = waId;
        this.lastMessageTime = lastMessageTime;
        this.messageCount = messageCount;
        this.isActive=isActive;
        this.userName=userName;

    }
    public String getUserName() {
        return userName;
    }
    public int getMessageCount() {
        return messageCount;
    }

    public boolean isActive() {
        return isActive;
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