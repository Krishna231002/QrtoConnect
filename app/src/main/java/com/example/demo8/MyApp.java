package com.example.demo8;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Firebase Database persistence
       FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
