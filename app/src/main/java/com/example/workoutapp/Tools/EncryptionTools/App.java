package com.example.workoutapp.Tools.EncryptionTools;

import android.app.Application;

import net.sqlcipher.database.SQLiteDatabase;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase.loadLibs(this);
    }
}
