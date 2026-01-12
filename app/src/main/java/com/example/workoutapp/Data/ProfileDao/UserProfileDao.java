package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.USER_AGE;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_HEIGHT;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_PROFILE_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_IMAGE_PATH;

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

    // ========================= INSERT / UPDATE ========================= //

    /**
     * Вставка или обновление профиля.
     * Обрабатывает все поля, включая путь к фотографии профиля.
     */
    public void insertOrUpdateProfile(UserProfileModel newProfile) {
        if (newProfile == null) return;

        UserProfileModel current = getProfile();
        ContentValues values = new ContentValues();

        // Подготовка значений
        if (newProfile.getUserId() != 0) values.put(USER_ID, newProfile.getUserId());
        if (isValidString(newProfile.getUserName())) values.put(USER_NAME, newProfile.getUserName());
        if (newProfile.getUserHeight() > 0) values.put(USER_HEIGHT, newProfile.getUserHeight());
        if (newProfile.getUserAge() > 0) values.put(USER_AGE, newProfile.getUserAge());
        if (isValidString(newProfile.getUserImagePath())) values.put(USER_IMAGE_PATH, newProfile.getUserImagePath());

        if (current == null) {
            db.insert(USER_PROFILE_TABLE, null, values);
            Log.d("UserProfileDao", "Создана новая запись профиля");
        } else {
            ContentValues updatedValues = new ContentValues();
            for (String key : values.keySet()) {
                Object newVal = values.get(key);
                Object oldVal = getFieldValueByKey(current, key);
                if (newVal != null && !newVal.equals(oldVal)) {
                    updatedValues.put(key, newVal.toString());
                }
            }

            if (updatedValues.size() > 0) {
                db.update(USER_PROFILE_TABLE, updatedValues, null, null);
                Log.d("UserProfileDao", "Данные профиля обновлены");
            } else {
                Log.i("UserProfileDao", "Изменений не обнаружено");
            }
        }
    }

    // ========================= GET PROFILE ========================= //

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

    // ========================= OTHER METHODS ========================= //

    public void setExternalUserId(long externalId) {
        ContentValues values = new ContentValues();
        values.put(USER_ID, externalId);

        Cursor cursor = null;
        try {
            cursor = db.query(USER_PROFILE_TABLE, new String[]{USER_ID}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                db.update(USER_PROFILE_TABLE, values, null, null);
            } else {
                db.insert(USER_PROFILE_TABLE, null, values);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void clearProfile() {
        db.delete(USER_PROFILE_TABLE, null, null);
    }

    // ========================= HELPERS ========================= //

    private boolean isValidString(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private Object getFieldValueByKey(UserProfileModel profile, String key) {
        switch (key) {
            case USER_ID: return profile.getUserId();
            case USER_NAME: return profile.getUserName();
            case USER_HEIGHT: return profile.getUserHeight();
            case USER_AGE: return profile.getUserAge();
            case USER_IMAGE_PATH: return profile.getUserImagePath();
            default: return null;
        }
    }
}
