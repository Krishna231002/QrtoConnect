package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

public class BroadcastLastMessageModel implements Parcelable {

    public String senderId,message,date,type;

    public BroadcastLastMessageModel() {
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    protected BroadcastLastMessageModel(Parcel in) {
        senderId = in.readString();
        message = in.readString();
        date = in.readString();
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(senderId);
        dest.writeString(message);
        dest.writeString(date);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BroadcastLastMessageModel> CREATOR = new Creator<BroadcastLastMessageModel>() {
        @Override
        public BroadcastLastMessageModel createFromParcel(Parcel in) {
            return new BroadcastLastMessageModel(in);
        }

        @Override
        public BroadcastLastMessageModel[] newArray(int size) {
            return new BroadcastLastMessageModel[size];
        }
    };
}
