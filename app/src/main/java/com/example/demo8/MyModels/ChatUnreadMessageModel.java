package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatUnreadMessageModel implements Parcelable {
    public String senderId,message,date,senderRoom;
            public int unreadMessageCount;

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSenderRoom(String senderRoom) {
        this.senderRoom = senderRoom;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public ChatUnreadMessageModel() {
    }

    public ChatUnreadMessageModel(String senderId, int unreadMessageCount) {
        this.senderId = senderId;
        this.unreadMessageCount = unreadMessageCount;
    }

    protected ChatUnreadMessageModel(Parcel in) {
        senderId = in.readString();
        message = in.readString();
        date = in.readString();
        senderRoom = in.readString();
        unreadMessageCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(senderId);
        dest.writeString(message);
        dest.writeString(date);
        dest.writeString(senderRoom);
        dest.writeInt(unreadMessageCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChatUnreadMessageModel> CREATOR = new Creator<ChatUnreadMessageModel>() {
        @Override
        public ChatUnreadMessageModel createFromParcel(Parcel in) {
            return new ChatUnreadMessageModel(in);
        }

        @Override
        public ChatUnreadMessageModel[] newArray(int size) {
            return new ChatUnreadMessageModel[size];
        }
    };

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getSenderRoom() {
        return senderRoom;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }
}
