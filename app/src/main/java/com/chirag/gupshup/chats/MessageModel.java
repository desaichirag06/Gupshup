package com.chirag.gupshup.chats;

public class MessageModel {
    private String message, messageFrom, messageId, messageType;
    private long messageTime;


    public MessageModel() {
    }

    public MessageModel(String message, String messageFrom, String messageId, long messageTime, String messageType) {
        this.message = message;
        this.messageFrom = messageFrom;
        this.messageId = messageId;
        this.messageTime = messageTime;
        this.messageType = messageType;
    }


    @Override
    public String toString() {
        return "MessageModel{" +
                "message='" + message + '\'' +
                ", messageFrom='" + messageFrom + '\'' +
                ", messageId='" + messageId + '\'' +
                ", messageTime='" + messageTime + '\'' +
                ", messageType='" + messageType + '\'' +
                '}';
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
