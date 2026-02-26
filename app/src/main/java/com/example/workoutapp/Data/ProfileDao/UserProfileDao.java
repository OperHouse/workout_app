package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.USER_AGE;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_FIREBASE_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_HEIGHT;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_IMAGE_PATH;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_PROFILE_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Models.ProfileModels.UserProfileModel;

import net.sqlcipher.database.SQLiteDatabase;

public class UserProfileDao {

    private final SQLiteDatabase db;

    public UserProfileDao(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Вставка или обновление профиля в локальной БД.
     */
    public void insertOrUpdateProfile(UserProfileModel newProfile) {
        if (newProfile == null) return;

        UserProfileModel current = getProfile();
        ContentValues values = new ContentValues();

        if (newProfile.getUserId() != 0) values.put(USER_ID, newProfile.getUserId());
        if (isValidString(newProfile.getUserName())) values.put(USER_NAME, newProfile.getUserName());
        if (newProfile.getUserHeight() > 0) values.put(USER_HEIGHT, newProfile.getUserHeight());
        if (newProfile.getUserAge() > 0) values.put(USER_AGE, newProfile.getUserAge());
        if (isValidString(newProfile.getUserImagePath())) values.put(USER_IMAGE_PATH, newProfile.getUserImagePath());

        if (current == null) {
            db.insert(USER_PROFILE_TABLE, null, values);
            Log.d("UserProfileDao", "Создана новая запись профиля");
        } else {
            db.update(USER_PROFILE_TABLE, values, null, null);
            Log.d("UserProfileDao", "Данные профиля обновлены локально");
        }
    }

    public UserProfileModel getProfile() {
        Cursor cursor = null;
        UserProfileModel profile = null;
        try {
            cursor = db.query(USER_PROFILE_TABLE, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                profile = new UserProfileModel(
                        cursor.getLong(cursor.getColumnIndexOrThrow(USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(USER_HEIGHT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(USER_AGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(USER_IMAGE_PATH))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return profile;
    }

    public void updateFirebaseId(String uid) {
        if (uid == null) return;
        ContentValues values = new ContentValues();
        values.put(USER_FIREBASE_ID, uid);
        if (getProfile() != null) {
            db.update(USER_PROFILE_TABLE, values, null, null);
        } else {
            db.insert(USER_PROFILE_TABLE, null, values);
        }
    }

    public String getFirebaseId() {
        String fbId = null;
        Cursor cursor = null;
        try {
            cursor = db.query(USER_PROFILE_TABLE, new String[]{USER_FIREBASE_ID}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(USER_FIREBASE_ID);
                if (index != -1) fbId = cursor.getString(index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return fbId;
    }

    public void clearProfile() {
        db.delete(USER_PROFILE_TABLE, null, null);
    }

    private boolean isValidString(String s) {
        return s != null && !s.trim().isEmpty();
    }
}