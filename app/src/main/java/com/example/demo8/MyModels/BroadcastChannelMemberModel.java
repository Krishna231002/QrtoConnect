package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BroadcastChannelMemberModel implements Parcelable {
    public String id,role,token;

    public BroadcastChannelMemberModel(String id, String role, String token) {
        this.id = id;
        this.role = role;
        this.token = token;
    }

    public BroadcastChannelMemberModel() {
    }

    protected BroadcastChannelMemberModel(Parcel in) {
        id = in.readString();
        role = in.readString();
        token = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(role);
        dest.writeString(token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BroadcastChannelMemberModel> CREATOR = new Creator<BroadcastChannelMemberModel>() {
        @Override
        public BroadcastChannelMemberModel createFromParcel(Parcel in) {
            return new BroadcastChannelMemberModel(in);
        }

        @Override
        public BroadcastChannelMemberModel[] newArray(int size) {
            return new BroadcastChannelMemberModel[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
