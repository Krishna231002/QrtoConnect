package com.example.demo8.Interface;

import com.example.demo8.Users;

public interface ContactItemInterface {

    void onContactClick(Users users, int position, boolean isSelect);

    void deselectContact(Users users);

}
