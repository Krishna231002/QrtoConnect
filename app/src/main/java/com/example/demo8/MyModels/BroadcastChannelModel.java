package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class BroadcastChannelModel implements Parcelable {
    public String adminId,adminName,channelId,channelImage,channelName,role;

    public List<BroadcastChannelMemberModel> members;
    public boolean isAdmin=true;

    public BroadcastChannelModel() {
    }

    protected BroadcastChannelModel(Parcel in) {
        adminId = in.readString();
        adminName = in.readString();
        channelId = in.readString();
        channelImage = in.readString();
        channelName = in.readString();
        role = in.readString();
        members = in.createTypedArrayList(BroadcastChannelMemberModel.CREATOR);
        isAdmin = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(adminId);
        dest.writeString(adminName);
        dest.writeString(channelId);
        dest.writeString(channelImage);
        dest.writeString(channelName);
        dest.writeString(role);
        dest.writeTypedList(members);
        dest.writeByte((byte) (isAdmin ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BroadcastChannelModel> CREATOR = new Creator<BroadcastChannelModel>() {
        @Override
        public BroadcastChannelModel createFromParcel(Parcel in) {
            return new BroadcastChannelModel(in);
        }

        @Override
        public BroadcastChannelModel[] newArray(int size) {
            return new BroadcastChannelModel[size];
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

    public List<BroadcastChannelMemberModel> getMembers() {
        return members;
    }

    public void setMembers(List<BroadcastChannelMemberModel> members) {
        this.members = members;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
