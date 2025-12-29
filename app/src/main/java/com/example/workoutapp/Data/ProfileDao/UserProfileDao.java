package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.USER_AGE;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_HEIGHT;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_PROFILE_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;

public class UserProfileDao {

    private final AppDataBase dbHelper;

    public UserProfileDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // ========================= INSERT / UPDATE ========================= //

    /**
     * Вставка или обновление профиля.
     * Если поля пустые — не трогаем.
     * Если значения совпадают с текущими — не трогаем.
     * Если таблица пуста — создаём первую запись.
     */
    public void insertOrUpdateProfile(UserProfileModel newProfile) {


        // Проверим, есть ли уже запись
        UserProfileModel current = getProfile();

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        // Если таблица пуста — вставляем новую строку
        if (current == null) {
            if (newProfile.getUserId() != 0)
                values.put(USER_ID, newProfile.getUserId());
            if (isValidString(newProfile.getUserName()))
                values.put(USER_NAME, newProfile.getUserName());
            if (newProfile.getUserHeight() > 0)
                values.put(USER_HEIGHT, newProfile.getUserHeight());
            if (newProfile.getUserAge() > 0)
                values.put(USER_AGE, newProfile.getUserAge());

             db.insert(USER_PROFILE_TABLE, null, values);
        } else {
            // Если запись есть — обновляем только изменённые и непустые поля
            if (isValidString(newProfile.getUserName()) &&
                    !newProfile.getUserName().equals(current.getUserName())) {
                values.put(USER_NAME, newProfile.getUserName());
            }

            if (newProfile.getUserHeight() > 0 &&
                    newProfile.getUserHeight() != current.getUserHeight()) {
                values.put(USER_HEIGHT, newProfile.getUserHeight());
            }

            if (newProfile.getUserAge() > 0 &&
                    newProfile.getUserAge() != current.getUserAge()) {
                values.put(USER_AGE, newProfile.getUserAge());
            }

            if (newProfile.getUserId() != 0 &&
                    newProfile.getUserId() != current.getUserId()) {
                values.put(USER_ID, newProfile.getUserId());
            }

            if (values.size() > 0) {
                db.update(USER_PROFILE_TABLE, values, null, null);
            } else {
                Log.i("UserProfileDao", "Нет изменений для обновления профиля.");
            }
        }

        db.close();
    }

    // ========================= SET USER ID ========================= //

    /**
     * Устанавливает внешний user_id (например, из Firebase).
     * Если таблицы нет или она пуста — создаёт запись с этим ID.
     */
    public void setExternalUserId(long externalId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(
                USER_PROFILE_TABLE,
                new String[]{USER_ID},
                null,
                null,
                null,
                null,
                null
        );

        ContentValues values = new ContentValues();
        values.put(USER_ID, externalId);

        if (cursor.moveToFirst()) {
            db.update(USER_PROFILE_TABLE, values, null, null);
        } else {
            db.insert(USER_PROFILE_TABLE, null, values);
        }

        cursor.close();
        db.close();
    }

    // ========================= GET PROFILE ========================= //

    /**
     * Получает текущий профиль (всегда одну строку).
     */
    public UserProfileModel getProfile() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        UserProfileModel profile = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    USER_PROFILE_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                profile = new UserProfileModel(
                        cursor.getLong(cursor.getColumnIndexOrThrow(USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(USER_HEIGHT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(USER_AGE))
                );
            }

        } catch (android.database.sqlite.SQLiteException e) {
            if (e.getMessage() != null && e.getMessage().contains("no such table")) {
                Log.w("UserProfileDao", "Таблица user_profile_table отсутствует, возвращаем null");
                return null;
            }
            throw e;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return profile;
    }

    // ========================= CLEAR PROFILE ========================= //

    /**
     * Полная очистка таблицы (удаление всех параметров профиля).
     */
    public void clearProfile() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(USER_PROFILE_TABLE, null, null);
        db.close();
    }

    // ========================= EXPORT ========================= //

    /**
     * Подготовка данных для отправки во внешнюю базу данных.
     */
    public UserProfileModel exportProfileForExternalDb() {
        // просто получаем профиль — отправка реализуется на уровне сервиса/репозитория
        return getProfile();
    }

    // ========================= HELPERS ========================= //

    private boolean isValidString(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
