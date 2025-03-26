package com.example.demo8;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.demo8.MyModels.GroupLastMessageModel;
import com.example.demo8.MyModels.GroupMemberModel;

import java.util.List;

public class GroupModels implements Parcelable {

    public String id,adminId,adminName,createAt,image,name;

    public List<GroupMemberModel> members;

    public GroupLastMessageModel lastMessageModel;
    public boolean isAdmin=true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GroupMemberModel> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMemberModel> members) {
        this.members = members;
    }

    public GroupLastMessageModel getLastMessageModel() {
        return lastMessageModel;
    }

    public void setLastMessageModel(GroupLastMessageModel lastMessageModel) {
        this.lastMessageModel = lastMessageModel;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }



    public GroupModels() {
    }

    // getters and setters

    protected GroupModels(Parcel in) {
        id = in.readString();
        adminId = in.readString();
        adminName = in.readString();
        createAt = in.readString();
        image = in.readString();
        name = in.readString();
        members = in.createTypedArrayList(GroupMemberModel.CREATOR);
        lastMessageModel = in.readParcelable(GroupLastMessageModel.class.getClassLoader());
        isAdmin = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(adminId);
        dest.writeString(adminName);
        dest.writeString(createAt);
        dest.writeString(image);
        dest.writeString(name);
        dest.writeTypedList(members);
        dest.writeParcelable(lastMessageModel, flags);
        dest.writeByte((byte) (isAdmin ? 1 : 0));

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GroupModels> CREATOR = new Creator<GroupModels>() {
        @Override
        public GroupModels createFromParcel(Parcel in) {
            return new GroupModels(in);
        }

        @Override
        public GroupModels[] newArray(int size) {
            return new GroupModels[size];
        }
    };


}