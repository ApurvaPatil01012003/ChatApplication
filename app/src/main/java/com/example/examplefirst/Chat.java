package com.example.examplefirst;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Chat {
    private String message;
    private String sender;
    private long timestamp; // Store the message time in milliseconds


    public Chat(String message, String sender,long timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp=timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }
    // Format timestamp to display in a readable format (e.g., HH:mm or dd/MM/yyyy HH:mm)
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
