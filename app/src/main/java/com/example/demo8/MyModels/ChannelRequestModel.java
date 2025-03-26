package com.example.demo8.MyModels;

public class ChannelRequestModel {

    String reqUserId, reqChannelId,reqImage,reqName,reqChannelName,status,reqToken,creatorUserId;

    public ChannelRequestModel(String reqUserId, String reqChannelId, String reqImage, String reqName, String reqChannelName, String status, String reqToken, String creatorUserId) {
        this.reqUserId = reqUserId;
        this.reqChannelId = reqChannelId;
        this.reqImage = reqImage;
        this.reqName = reqName;
        this.reqChannelName = reqChannelName;
        this.status = status;
        this.reqToken = reqToken;
        this.creatorUserId = creatorUserId;
    }


    public String getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public String getReqUserId() {
        return reqUserId;
    }

    public void setReqUserId(String reqUserId) {
        this.reqUserId = reqUserId;
    }

    public String getReqToken() {
        return reqToken;
    }

    public void setReqToken(String reqToken) {
        this.reqToken = reqToken;
    }

    public String getReqChannelId() {
        return reqChannelId;
    }

    public void setReqChannelId(String reqChannelId) {
        this.reqChannelId = reqChannelId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ChannelRequestModel(){

    }
    public String getReqImage() {
        return reqImage;
    }

    public void setReqImage(String reqImage) {
        this.reqImage = reqImage;
    }

    public String getReqName() {
        return reqName;
    }

    public void setReqName(String reqName) {
        this.reqName = reqName;
    }

    public String getReqChannelName() {
        return reqChannelName;
    }

    public void setReqChannelName(String reqChannelName) {
        this.reqChannelName = reqChannelName;
    }
}
