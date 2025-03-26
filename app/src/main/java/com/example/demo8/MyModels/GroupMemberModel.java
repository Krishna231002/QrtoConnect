package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupMemberModel implements Parcelable {

    public String id,role,token,mobile;


    public GroupMemberModel(String id, String role, String token) {
        this.id = id;
        this.role = role;
        this.token = token;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public GroupMemberModel() {
    }

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





    protected GroupMemberModel(Parcel in) {
        id = in.readString();
        role = in.readString();
        token = in.readString();
        mobile = in.readString();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(role);
        dest.writeString(token);
        dest.writeString(mobile);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GroupMemberModel> CREATOR = new Creator<GroupMemberModel>() {
        @Override
        public GroupMemberModel createFromParcel(Parcel in) {
            return new GroupMemberModel(in);
        }

        @Override
        public GroupMemberModel[] newArray(int size) {
            return new GroupMemberModel[size];
        }
    };
}
