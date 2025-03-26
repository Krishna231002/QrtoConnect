package com.example.demo8;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send the token to your server, here is where you can do it.
        sendRegistrationToServer(token);


    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }
}