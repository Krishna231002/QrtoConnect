package com.example.demo8;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class AllBroadcastChannelModel implements Parcelable{

    String adminId,adminName,channelId,channelImage,channelName,role,token;

    public AllBroadcastChannelModel(String adminId, String adminName, String channelId, String channelImage, String channelName, String role) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.channelId = channelId;
        this.channelImage = channelImage;
        this.channelName = channelName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AllBroadcastChannelModel() {
    }

    protected AllBroadcastChannelModel(Parcel in) {
        adminId = in.readString();
        adminName = in.readString();
        channelId = in.readString();
        channelImage = in.readString();
        channelName = in.readString();
        role = in.readString();
    }

    public static final Creator<AllBroadcastChannelModel> CREATOR = new Creator<AllBroadcastChannelModel>() {
        @Override
        public AllBroadcastChannelModel createFromParcel(Parcel in) {
            return new AllBroadcastChannelModel(in);
        }

        @Override
        public AllBroadcastChannelModel[] newArray(int size) {
            return new AllBroadcastChannelModel[size];
        }
    };

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelImage() {
        return channelImage;
    }

    public void setChannelImage(String channelImage) {
        this.channelImage = channelImage;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(adminId);
        dest.writeString(adminName);
        dest.writeString(channelId);
        dest.writeString(channelImage);
        dest.writeString(channelName);
        dest.writeString(role);
    }
}
