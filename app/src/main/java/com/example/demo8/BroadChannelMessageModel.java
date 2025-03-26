package com.example.demo8;

public class BroadChannelMessageModel {

    String type,message,date,senderId,name,messageId;

    Boolean isImageDownloaded=false;
    public BroadChannelMessageModel(String type, String message, String date, String senderId, String name, String messageId) {
        this.type = type;
        this.message = message;
        this.date = date;
        this.senderId = senderId;
        this.name = name;
        this.messageId = messageId;
    }


    public BroadChannelMessageModel() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Boolean getImageDownloaded() {
        return isImageDownloaded;
    }

    public void setImageDownloaded(Boolean imageDownloaded) {
        isImageDownloaded = imageDownloaded;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
