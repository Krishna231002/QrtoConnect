package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BroadcastChannelLastMsg implements Parcelable {
    public String senderId,message,date;

    public BroadcastChannelLastMsg() {
    }

    protected BroadcastChannelLastMsg(Parcel in) {
        senderId = in.readString();
        message = in.readString();
        date = in.readString();
    }

    public static final Creator<BroadcastChannelLastMsg> CREATOR = new Creator<BroadcastChannelLastMsg>() {
        @Override
        public BroadcastChannelLastMsg createFromParcel(Parcel in) {
            return new BroadcastChannelLastMsg(in);
        }

        @Override
        public BroadcastChannelLastMsg[] newArray(int size) {
            return new BroadcastChannelLastMsg[size];
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
