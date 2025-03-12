package com.example.examplefirst;


import android.util.Log;

public class ChatMessage {
    private String waId;      // Non-null => Outgoing
    private String sender;    // Non-null => Incoming
    private String messageBody;
    private String fileUrl;
    private String imageUrl;
    private String latitude;
    private String longitude;


    public String getLatitude() {

        return latitude != null ? latitude.trim() : "";


    }

    public void setLatitude(String latitude) {

        this.latitude = latitude;
    }

    public String getLongitude() {
//        Log.d("ChatMessage", "Returning longitude: " + longitude);
//        return longitude;
        return longitude != null ? longitude.trim() : "";
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }



    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private String messageType;
    private String timestamp;

    // Getters and setters...
    public String getWaId() { return waId; }
    public void setWaId(String waId) { this.waId = waId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    // Convert the UNIX timestamp in seconds to a readable date/time
    public String getFormattedTimestamp() {
        try {
            long ts = Long.parseLong(timestamp) * 1000L;
            java.util.Date date = new java.util.Date(ts);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yy HH:mm", java.util.Locale.getDefault());
            return sdf.format(date);
        } catch (NumberFormatException e) {
            return "Invalid Timestamp";
        }
    }
}
