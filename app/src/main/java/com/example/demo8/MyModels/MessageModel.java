package com.example.demo8.MyModels;

public class MessageModel {

    String uId,message,messageId,type;
    Boolean isSelected=false,isImageDownloaded=false;
    Long timestamp;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MessageModel(String uId, String message, String type) {
        this.uId = uId;
        this.message = message;
        this.type = type;
    }

    public MessageModel(String uId, String message, Long timestamp, String type) {
        this.uId = uId;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    public MessageModel(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    // getter for isSelected
    public boolean isSelected() {
        return isSelected;
    }

    // setter for isSelected
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public MessageModel(String uId, String message, Long timestamp) {
        this.uId = uId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public MessageModel(String uId, String message) {
        this.uId = uId;
        this.message = message;
    }

    public MessageModel() {
    }

    public Boolean getImageDownloaded() {
        return isImageDownloaded;
    }

    public void setImageDownloaded(Boolean imageDownloaded) {
        isImageDownloaded = imageDownloaded;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
