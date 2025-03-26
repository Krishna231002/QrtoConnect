package com.example.demo8;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.example.demo8.MyModels.ChatLastMessageModel;
import com.example.demo8.MyModels.ChatUnreadMessageModel;
import com.example.demo8.MyModels.GroupLastMessageModel;

import de.hdodenhof.circleimageview.CircleImageView;

public class Users {
    String firstname,lastname, mobile, email, companyName,imageURL,lastMessage,status,id,typing,token,fullname,password;
    int unreadMessage;
    public ChatLastMessageModel chatLastMessageModel;
    public ChatUnreadMessageModel chatUnreadMessageModel;

    public ChatUnreadMessageModel getChatUnreadMessageModel() {
        return chatUnreadMessageModel;
    }

    public void setChatUnreadMessageModel(ChatUnreadMessageModel chatUnreadMessageModel) {
        this.chatUnreadMessageModel = chatUnreadMessageModel;
    }

    public int getUnreadMessage() {
        return unreadMessage;
    }

    public void setUnreadMessage(int unreadMessage) {
        this.unreadMessage = unreadMessage;
    }



    public ChatLastMessageModel getChatLastMessageModel() {
        return chatLastMessageModel;
    }

    public void setChatLastMessageModel(ChatLastMessageModel chatLastMessageModel) {
        this.chatLastMessageModel = chatLastMessageModel;
    }

    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public Users(String firstname, String lastname, String mobile, String email, String imageURL, String status, String id, String typing, String token, String fullname, String password,String companyName) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.mobile = mobile;
        this.email = email;
        this.imageURL = imageURL;
        this.status = status;
        this.id = id;
        this.typing = typing;
        this.token = token;
        this.fullname = fullname;
        this.password = password;
        this.companyName = companyName;
    }

    public Users(String firstname, String lastname, String mobile, String email, String imageURL, String status, String id, String typing, String token, String fullname, String password) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.mobile = mobile;
        this.email = email;
        this.imageURL = imageURL;
        this.status = status;
        this.id = id;
        this.typing = typing;
        this.token = token;
        this.fullname = fullname;
        this.password = password;
    }
    public Users(String firstname, String lastname, String mobile, String email, String status, String id, String typing, String token, String fullname,String companyName) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.mobile = mobile;
        this.email = email;
        this.imageURL = imageURL;
        this.status = status;
        this.id = id;
        this.typing = typing;
        this.token = token;
        this.fullname = fullname;
        this.password = password;
        this.companyName = companyName;
    }

    public String getTyping() {
        return typing;
    }

    public void setTyping(String typing) {
        this.typing = typing;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getId() {
        return id;
    }

    public void setId(String key) {
        this.id = key;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Users() {
    }

    public String getFirstName() {
        return firstname;
    }

    public void setFirstName(String firstName) {
        this.firstname = firstName;
    }

    public String getLastName() {
        return lastname;
    }

    public void setLastName(String lastName) {
        this.lastname = lastName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(@NonNull CircleImageView view, @NonNull String image) {
        Glide.with(view.getContext()).load(image).into(view);
    }

    @BindingAdapter("imageChat")
    public static void loadImage(@NonNull ImageView view, @NonNull String image) {

        Glide.with(view.getContext()).load(image).into(view);

    }
}
