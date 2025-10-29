package com.example.workoutapp.Data.ProfileDao;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;

public class UserProfileDao {
    private final AppDataBase dbHelper;

    public UserProfileDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавляет новую запись профиля (ID должен быть 1 для однопользовательского режима)
    public long insertProfile(UserProfileModel profile) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // ID не ставим, т.к. он AUTOINCREMENT
        values.put(AppDataBase.USER_NAME, profile.getUserName());
        values.put(AppDataBase.USER_HEIGHT, profile.getUserHeight());
        values.put(AppDataBase.USER_AGE, profile.getUserAge()); // Поле, хранящее дату рождения или возраст

        long id = db.insert(AppDataBase.USER_PROFILE_TABLE, null, values);
        db.close();
        return id;
    }

    public void updateProfile(UserProfileModel profile) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppDataBase.USER_NAME, profile.getUserName());
        values.put(AppDataBase.USER_HEIGHT, profile.getUserHeight());
        values.put(AppDataBase.USER_AGE, profile.getUserAge());

        db.update(
                AppDataBase.USER_PROFILE_TABLE,
                values,
                AppDataBase.USER_ID + " = ?",
                new String[]{String.valueOf(profile.getUserId())}
        );

        db.close();
    }

    // Получение текущего профиля (по ID)
    public UserProfileModel getProfileById(long userId) {
        // Получение базы данных для чтения
        // ВНИМАНИЕ: Вызов getReadableDatabase() может запустить AppDataBase.onCreate()
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        UserProfileModel profile = null;
        Cursor cursor = null;

        try {
            // Здесь выполняется SQL-запрос SELECT.
            // Если таблица еще не создана (из-за race condition), здесь возникнет ошибка.
            cursor = db.query(
                    AppDataBase.USER_PROFILE_TABLE,
                    null, // Все столбцы
                    AppDataBase.USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null,
                    null,
                    null
            );

            // --- Логика чтения данных из курсора (неизменна) ---
            if (cursor != null && cursor.moveToFirst()) {
                profile = new UserProfileModel(
                        cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.USER_NAME)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(AppDataBase.USER_HEIGHT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.USER_AGE))
                );
                cursor.close();
            }
            // ---------------------------------------------------

        } catch (android.database.sqlite.SQLiteException e) {
            // *** КЛЮЧЕВОЙ МОМЕНТ: ПЕРЕХВАТ ОШИБКИ "no such table" ***
            if (e.getMessage() != null && e.getMessage().contains("no such table")) {
                // Эта ошибка означает, что таблица еще не успела создаться.
                // Это ожидаемо при race condition на реальном устройстве.
                android.util.Log.w("UserProfileDao", "Таблица user_profile_table временно отсутствует. Возвращаем NULL.");
                return null; // Возвращаем null, чтобы предотвратить краш приложения
            }
            // Если это другая, неожиданная ошибка SQLite, перебрасываем ее.
            throw e;
        } finally {
            if (cursor != null) cursor.close();
            // ВНИМАНИЕ: Не закрывайте базу данных, если она используется в других потоках.
            // Если dbHelper - Singleton, закрытие здесь может вызвать ошибки.
            // Но так как у вас SQLiteOpenHelper, db.close() тут остается.
            db.close();
        }

        return profile;
    }
}
