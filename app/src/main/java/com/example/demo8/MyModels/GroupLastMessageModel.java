package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupLastMessageModel implements Parcelable {
    public String senderId,message,date,type;


    public GroupLastMessageModel() {
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

    protected GroupLastMessageModel(Parcel in) {
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

    public static final Creator<GroupLastMessageModel> CREATOR = new Creator<GroupLastMessageModel>() {
        @Override
        public GroupLastMessageModel createFromParcel(Parcel in) {
            return new GroupLastMessageModel(in);
        }

        @Override
        public GroupLastMessageModel[] newArray(int size) {
            return new GroupLastMessageModel[size];
        }
    };
}
