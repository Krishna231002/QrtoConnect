package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ChatLastMessageModel implements Parcelable {

    public String senderId,message,date;

    public ChatLastMessageModel() {
    }

    protected ChatLastMessageModel(Parcel in) {
        senderId = in.readString();
        message = in.readString();
        date = in.readString();
    }

    public static final Creator<ChatLastMessageModel> CREATOR = new Creator<ChatLastMessageModel>() {
        @Override
        public ChatLastMessageModel createFromParcel(Parcel in) {
            return new ChatLastMessageModel(in);
        }

        @Override
        public ChatLastMessageModel[] newArray(int size) {
            return new ChatLastMessageModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(senderId);
        dest.writeString(message);
        dest.writeString(date);
    }
}
