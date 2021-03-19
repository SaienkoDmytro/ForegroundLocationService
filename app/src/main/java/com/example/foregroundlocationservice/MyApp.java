package com.example.foregroundlocationservice;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPref.init(getApplicationContext());
    }
}