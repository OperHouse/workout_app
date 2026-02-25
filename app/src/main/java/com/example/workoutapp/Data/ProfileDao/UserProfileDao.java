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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

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

        String uid = FirebaseAuth.getInstance().getUid(); // Получаем текущий ID из Firebase
        UserProfileModel current = getProfile();
        ContentValues values = new ContentValues();

        // Подготовка значений для SQLite
        if (newProfile.getUserId() != 0) values.put(USER_ID, newProfile.getUserId());
        if (isValidString(newProfile.getUserName()))
            values.put(USER_NAME, newProfile.getUserName());
        if (newProfile.getUserHeight() > 0) values.put(USER_HEIGHT, newProfile.getUserHeight());
        if (newProfile.getUserAge() > 0) values.put(USER_AGE, newProfile.getUserAge());
        if (isValidString(newProfile.getUserImagePath()))
            values.put(USER_IMAGE_PATH, newProfile.getUserImagePath());

        // Привязываем Firebase UID к локальной записи
        if (uid != null) values.put(USER_FIREBASE_ID, uid);

        if (current == null) {
            db.insert(USER_PROFILE_TABLE, null, values);
            Log.d("UserProfileDao", "Создана новая запись профиля");
        } else {
            db.update(USER_PROFILE_TABLE, values, null, null); // Упростил обновление для надежности
            Log.d("UserProfileDao", "Данные профиля обновлены");
        }

        if (uid != null) {
            Map<String, Object> cloudData = new HashMap<>();
            // Отправляем в облако только важные данные (картинку лучше хранить в Firebase Storage, а не Firestore)
            cloudData.put("name", newProfile.getUserName());
            cloudData.put("height", newProfile.getUserHeight());
            cloudData.put("age", newProfile.getUserAge());
            cloudData.put("last_sync", System.currentTimeMillis());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid) // Документ будет называться так же, как твой UID
                    .set(cloudData, SetOptions.merge()) // merge не затрет старые данные (например, тренировки)
                    .addOnSuccessListener(aVoid -> Log.d("CloudSync", "Профиль синхронизирован с Firestore"))
                    .addOnFailureListener(e -> Log.e("CloudSync", "Ошибка синхронизации профиля", e));
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

    /**
     * ПОЛНАЯ СИНХРОНИЗАЦИЯ ПРОФИЛЯ:
     * 1. Если в облаке данных нет -> отправляем локальные.
     * 2. Если на телефоне данных нет -> скачиваем из облака.
     */
    public void syncProfileWithCloud() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    UserProfileModel localProfile = getProfile();

                    if (documentSnapshot.exists()) {
                        // --- ДАННЫЕ В ОБЛАКЕ ЕСТЬ ---
                        if (localProfile == null || !isValidString(localProfile.getUserName())) {
                            // На телефоне пусто или имя не задано -> забираем из облака
                            UserProfileModel cloudProfile = new UserProfileModel(
                                    0,
                                    documentSnapshot.getString("name"),
                                    documentSnapshot.getDouble("height") != null ? documentSnapshot.getDouble("height").floatValue() : 0,
                                    documentSnapshot.getLong("age") != null ? documentSnapshot.getLong("age").intValue() : 0,
                                    null
                            );
                            insertOrUpdateProfile(cloudProfile);
                            Log.d("Sync", "Профиль скачан из облака");
                        } else {
                            // Данные есть и там, и там. Можно сравнить last_sync,
                            // но обычно после логина приоритет отдается облаку,
                            // а при редактировании - телефону.
                            Log.d("Sync", "Данные профиля уже актуальны");
                        }
                    } else {
                        // --- В ОБЛАКЕ ПУСТО ---
                        if (localProfile != null && isValidString(localProfile.getUserName())) {
                            // На телефоне данные есть -> пушим в облако
                            insertOrUpdateProfile(localProfile);
                            Log.d("Sync", "Локальный профиль отправлен в пустое облако");
                        }
                    }
                });
    }

    // Добавляем метод для проверки наличия привязанного ID
    public String getFirebaseId() {
        String fbId = null;
        Cursor cursor = null;
        try {
            // Запрашиваем только колонку с ID
            cursor = db.query(USER_PROFILE_TABLE, new String[]{USER_FIREBASE_ID}, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(USER_FIREBASE_ID);
                if (index != -1) {
                    fbId = cursor.getString(index);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return fbId;
    }
}
