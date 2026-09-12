package com.may25.app.models;

public class Message {
    private String messageId;
    private String senderId;
    private String message;
    private String type; // "text" or "image"
    private long timestamp;

    public Message() {}

    public Message(String messageId, String senderId, String message, String type, long timestamp) {
        this.messageId = messageId;
        this.senderId  = senderId;
        this.message   = message;
        this.type      = type;
        this.timestamp = timestamp;
    }

    public String getMessageId() { return messageId; }
    public String getSenderId()  { return senderId; }
    public String getMessage()   { return message; }
    public String getType()      { return type; }
    public long getTimestamp()   { return timestamp; }

    public void setMessageId(String messageId) { this.messageId = messageId; }
    public void setSenderId(String senderId)    { this.senderId = senderId; }
    public void setMessage(String message)      { this.message = message; }
    public void setType(String type)            { this.type = type; }
    public void setTimestamp(long timestamp)    { this.timestamp = timestamp; }
}
