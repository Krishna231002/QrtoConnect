package com.example.demo8.MyModels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Contact implements Parcelable {
    String contactEmail,contactFirstName,contactLastName,contactMobile,contactCompanyName, contactNotes,imageURL,key;
    Boolean contactFavorite;

    public Contact(String contactEmail, String contactFirstName, String contactLastName, String contactMobile, String contactCompanyName, String contactNotes, String imageURL, String key, Boolean contactFavorite) {
        this.contactEmail = contactEmail;
        this.contactFirstName = contactFirstName;
        this.contactLastName = contactLastName;
        this.contactMobile = contactMobile;
        this.contactCompanyName = contactCompanyName;
        this.contactNotes = contactNotes;
        this.imageURL = imageURL;
        this.key = key;
        this.contactFavorite = contactFavorite;
    }

    public Contact(Boolean contactFavorite) {
        this.contactFavorite = contactFavorite;
    }

    public Contact() {

    }


    protected Contact(Parcel in) {
        contactEmail = in.readString();
        contactFirstName = in.readString();
        contactLastName = in.readString();
        contactMobile = in.readString();
        contactCompanyName = in.readString();
        contactNotes = in.readString();
        imageURL = in.readString();
        key = in.readString();
        contactFavorite = Boolean.valueOf(in.readString());
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactFirstName() {
        return contactFirstName;
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public String getContactCompanyName() {
        return contactCompanyName;
    }

    public void setContactCompanyName(String contactCompanyName) {
        this.contactCompanyName = contactCompanyName;
    }

    public String getContactNotes() {
        return contactNotes;
    }

    public void setContactNotes(String contactNotes) {
        this.contactNotes = contactNotes;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getContactFavorite() {
        return contactFavorite;
    }

    public void setContactFavorite(Boolean contactFavorite) {
        this.contactFavorite = contactFavorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(contactEmail);
        dest.writeString(contactFirstName);
        dest.writeString(contactLastName);
        dest.writeString(contactMobile);
        dest.writeString(contactCompanyName);
        dest.writeString(contactNotes);
        dest.writeString(imageURL);
        dest.writeString(key);
        dest.writeString(String.valueOf(contactFavorite));
    }
}
