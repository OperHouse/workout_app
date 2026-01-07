package com.example.workoutapp.Data.ProfileDao;

// Статические импорты констант из вашего класса базы данных
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_AGE;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_HEIGHT;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_PROFILE_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.USER_IMAGE_PATH; // Убедитесь, что эта константа есть в AppDataBase

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
     * Обрабатывает все поля, включая путь к фотографии профиля.
     */
    public void insertOrUpdateProfile(UserProfileModel newProfile) {
        // Получаем текущее состояние из БД для сравнения
        UserProfileModel current = getProfile();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Если профиля еще нет в базе — создаем новую запись
        if (current == null) {
            if (newProfile.getUserId() != 0)
                values.put(USER_ID, newProfile.getUserId());
            if (isValidString(newProfile.getUserName()))
                values.put(USER_NAME, newProfile.getUserName());
            if (newProfile.getUserHeight() > 0)
                values.put(USER_HEIGHT, newProfile.getUserHeight());
            if (newProfile.getUserAge() > 0)
                values.put(USER_AGE, newProfile.getUserAge());
            if (isValidString(newProfile.getUserImagePath()))
                values.put(USER_IMAGE_PATH, newProfile.getUserImagePath());

            db.insert(USER_PROFILE_TABLE, null, values);
            Log.d("UserProfileDao", "Создана новая запись профиля");
        } else {
            // Если запись существует — обновляем только те поля, которые изменились
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

            // Обновляем путь к фото, если он изменился или был добавлен
            if (isValidString(newProfile.getUserImagePath()) &&
                    !newProfile.getUserImagePath().equals(current.getUserImagePath())) {
                values.put(USER_IMAGE_PATH, newProfile.getUserImagePath());
            }

            if (values.size() > 0) {
                // Обновляем единственную запись в таблице (whereClause null)
                db.update(USER_PROFILE_TABLE, values, null, null);
                Log.d("UserProfileDao", "Данные профиля обновлены");
            } else {
                Log.i("UserProfileDao", "Изменений не обнаружено");
            }
        }

        db.close();
    }

    // ========================= GET PROFILE ========================= //

    /**
     * Извлекает данные профиля из БД.
     */
    public UserProfileModel getProfile() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        UserProfileModel profile = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    USER_PROFILE_TABLE,
                    null, // Получаем все колонки
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // Создаем модель, используя конструктор с 5 параметрами
                profile = new UserProfileModel(
                        cursor.getLong(cursor.getColumnIndexOrThrow(USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(USER_HEIGHT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(USER_AGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(USER_IMAGE_PATH)) // Читаем путь к фото
                );
            }

        } catch (android.database.sqlite.SQLiteException e) {
            if (e.getMessage() != null && e.getMessage().contains("no such table")) {
                Log.w("UserProfileDao", "Таблица отсутствует");
                return null;
            }
            throw e;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return profile;
    }

    // ========================= ДРУГИЕ МЕТОДЫ ========================= //

    public void setExternalUserId(long externalId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID, externalId);

        Cursor cursor = db.query(USER_PROFILE_TABLE, new String[]{USER_ID}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            db.update(USER_PROFILE_TABLE, values, null, null);
        } else {
            db.insert(USER_PROFILE_TABLE, null, values);
        }
        cursor.close();
        db.close();
    }

    public void clearProfile() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(USER_PROFILE_TABLE, null, null);
        db.close();
    }

    // ========================= HELPERS ========================= //

    private boolean isValidString(String s) {
        return s != null && !s.trim().isEmpty();
    }
}