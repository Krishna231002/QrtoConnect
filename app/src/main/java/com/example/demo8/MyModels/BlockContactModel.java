package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

public class BlockContactModel implements Parcelable {
    public String blockEmail,blockFirstName,blockLastName,blockMobile,blockCompanyName, blockNotes,blockImageURL,blockKey;

    public BlockContactModel(String blockEmail, String blockFirstName, String blockLastName, String blockMobile, String blockCompanyName, String blockNotes, String blockImageURL, String blockKey) {
        this.blockEmail = blockEmail;
        this.blockFirstName = blockFirstName;
        this.blockLastName = blockLastName;
        this.blockMobile = blockMobile;
        this.blockCompanyName = blockCompanyName;
        this.blockNotes = blockNotes;
        this.blockImageURL = blockImageURL;
        this.blockKey = blockKey;
    }

    public BlockContactModel() {
    }

    public String getBlockEmail() {
        return blockEmail;
    }

    public void setBlockEmail(String blockEmail) {
        this.blockEmail = blockEmail;
    }

    public String getBlockFirstName() {
        return blockFirstName;
    }

    public void setBlockFirstName(String blockFirstName) {
        this.blockFirstName = blockFirstName;
    }

    public String getBlockLastName() {
        return blockLastName;
    }

    public void setBlockLastName(String blockLastName) {
        this.blockLastName = blockLastName;
    }

    public String getBlockMobile() {
        return blockMobile;
    }

    public void setBlockMobile(String blockMobile) {
        this.blockMobile = blockMobile;
    }

    public String getBlockCompanyName() {
        return blockCompanyName;
    }

    public void setBlockCompanyName(String blockCompanyName) {
        this.blockCompanyName = blockCompanyName;
    }

    public String getBlockNotes() {
        return blockNotes;
    }

    public void setBlockNotes(String blockNotes) {
        this.blockNotes = blockNotes;
    }

    public String getBlockImageURL() {
        return blockImageURL;
    }

    public void setBlockImageURL(String blockImageURL) {
        this.blockImageURL = blockImageURL;
    }

    public String getBlockKey() {
        return blockKey;
    }

    public void setBlockKey(String blockKey) {
        this.blockKey = blockKey;
    }

    protected BlockContactModel(Parcel in) {
        blockEmail = in.readString();
        blockFirstName = in.readString();
        blockLastName = in.readString();
        blockMobile = in.readString();
        blockCompanyName = in.readString();
        blockNotes = in.readString();
        blockImageURL = in.readString();
        blockKey = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(blockEmail);
        dest.writeString(blockFirstName);
        dest.writeString(blockLastName);
        dest.writeString(blockMobile);
        dest.writeString(blockCompanyName);
        dest.writeString(blockNotes);
        dest.writeString(blockImageURL);
        dest.writeString(blockKey);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BlockContactModel> CREATOR = new Creator<BlockContactModel>() {
        @Override
        public BlockContactModel createFromParcel(Parcel in) {
            return new BlockContactModel(in);
        }

        @Override
        public BlockContactModel[] newArray(int size) {
            return new BlockContactModel[size];
        }
    };
}
