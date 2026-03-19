package com.example.workoutapp.Data.WorkoutDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_EXERCISE_TABLE;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class BASE_EXERCISE_TABLE_DAO {

    private final SQLiteDatabase db;

    public BASE_EXERCISE_TABLE_DAO(SQLiteDatabase db) {
        this.db = db;
    }

    // =========================
    // Добавление упражнения (ОБНОВЛЕНО: добавлен UID)
    // =========================
    public long addExercise(BaseExModel exercise) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.BASE_EXERCISE_NAME, exercise.getBase_ex_name());
        values.put(AppDataBase.BASE_EXERCISE_TYPE, exercise.getBase_ex_type());
        values.put(AppDataBase.BASE_EXERCISE_BODY_TYPE, exercise.getBase_ex_bodyType());

        // Записываем UID в базу
        values.put(AppDataBase.BASE_EXERCISE_UID, exercise.getBase_ex_uid());

        return db.insert(BASE_EXERCISE_TABLE, null, values);
    }

    // =========================
    // Получение упражнения по ID (ОБНОВЛЕНО: читаем UID)
    // =========================
    public BaseExModel getExerciseById(long baseExId) {
        BaseExModel exercise = null;
        Cursor cursor = null;

        try {
            String[] columns = {
                    AppDataBase.BASE_EXERCISE_ID,
                    AppDataBase.BASE_EXERCISE_NAME,
                    AppDataBase.BASE_EXERCISE_TYPE,
                    AppDataBase.BASE_EXERCISE_BODY_TYPE,
                    AppDataBase.BASE_EXERCISE_UID // Добавили колонку
            };

            cursor = db.query(
                    BASE_EXERCISE_TABLE,
                    columns,
                    AppDataBase.BASE_EXERCISE_ID + " = ?",
                    new String[]{String.valueOf(baseExId)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                @SuppressLint("Range")
                long id = cursor.getLong(cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_ID));
                @SuppressLint("Range")
                String name = cursor.getString(cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_NAME));
                @SuppressLint("Range")
                String type = cursor.getString(cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_TYPE));
                @SuppressLint("Range")
                String bodyType = cursor.getString(cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_BODY_TYPE));
                @SuppressLint("Range")
                String uid = cursor.getString(cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_UID)); // Читаем UID

                // Используем конструктор с UID (создай его в модели, если еще нет, или используй сеттер)
                exercise = new BaseExModel(id, name, type, bodyType, uid);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return exercise;
    }

    // =========================
    // Получение всех упражнений (ОБНОВЛЕНО: читаем UID)
    // =========================
    public List<BaseExModel> getAllExercises() {
        List<BaseExModel> exercises = new ArrayList<>();
        Cursor cursor = null;

        try {
            String[] columns = {
                    AppDataBase.BASE_EXERCISE_ID,
                    AppDataBase.BASE_EXERCISE_NAME,
                    AppDataBase.BASE_EXERCISE_TYPE,
                    AppDataBase.BASE_EXERCISE_BODY_TYPE,
                    AppDataBase.BASE_EXERCISE_UID // Добавили колонку
            };

            cursor = db.query(
                    BASE_EXERCISE_TABLE,
                    columns,
                    null, null, null, null, null
            );

            int idIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_ID);
            int nameIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_NAME);
            int typeIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_TYPE);
            int bodyTypeIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_BODY_TYPE);
            int uidIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_UID); // Индекс UID

            while (cursor.moveToNext()) {
                BaseExModel exercise = new BaseExModel(
                        cursor.getLong(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(typeIndex),
                        cursor.getString(bodyTypeIndex),
                        cursor.getString(uidIndex) // Передаем UID в конструктор
                );
                exercises.add(exercise);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return exercises;
    }

    // =========================
    // Обновление упражнения (ОБНОВЛЕНО: добавлен UID)
    // =========================
    public void updateExercise(BaseExModel exercise) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.BASE_EXERCISE_NAME, exercise.getBase_ex_name());
        values.put(AppDataBase.BASE_EXERCISE_TYPE, exercise.getBase_ex_type());
        values.put(AppDataBase.BASE_EXERCISE_BODY_TYPE, exercise.getBase_ex_bodyType());
        values.put(AppDataBase.BASE_EXERCISE_UID, exercise.getBase_ex_uid()); // Обновляем UID

        db.update(
                BASE_EXERCISE_TABLE,
                values,
                AppDataBase.BASE_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(exercise.getBase_ex_id())}
        );
    }

    // Остальные методы (delete, getCount, isExerciseUidExists, deleteAll) остаются без изменений
    public void deleteExercise(long id) {
        db.delete(BASE_EXERCISE_TABLE, AppDataBase.BASE_EXERCISE_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public long getCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + BASE_EXERCISE_TABLE, null);
        long count = 0;
        if (cursor.moveToFirst()) count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    public boolean isExerciseUidExists(String uid) {
        if (uid == null || uid.isEmpty()) return false;
        Cursor cursor = null;
        try {
            cursor = db.query(BASE_EXERCISE_TABLE, new String[]{AppDataBase.BASE_EXERCISE_UID}, AppDataBase.BASE_EXERCISE_UID + " = ?", new String[]{uid}, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void deleteAllExercises() {
        db.delete(AppDataBase.BASE_EXERCISE_TABLE, null, null);
        db.delete("sqlite_sequence", "name = ?", new String[]{AppDataBase.BASE_EXERCISE_TABLE});
    }

    // =========================
    // Поиск ID базового упражнения по его названию
    // =========================
    public long getExerciseIdByName(String exerciseName) {
        if (exerciseName == null) return -1;

        long id = -1;
        String selection = AppDataBase.BASE_EXERCISE_NAME + " = ?";
        String[] selectionArgs = {exerciseName};

        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.BASE_EXERCISE_TABLE,
                    new String[]{AppDataBase.BASE_EXERCISE_ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.BASE_EXERCISE_ID));
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return id;
    }

    // =========================
    // Получение упражнения по UID (Добавь этот метод)
    // =========================
    public BaseExModel getExByUid(String uid) {
        if (uid == null || uid.isEmpty()) return null;

        BaseExModel exercise = null;
        Cursor cursor = null;

        try {
            String[] columns = {
                    AppDataBase.BASE_EXERCISE_ID,
                    AppDataBase.BASE_EXERCISE_NAME,
                    AppDataBase.BASE_EXERCISE_TYPE,
                    AppDataBase.BASE_EXERCISE_BODY_TYPE,
                    AppDataBase.BASE_EXERCISE_UID
            };

            cursor = db.query(
                    AppDataBase.BASE_EXERCISE_TABLE,
                    columns,
                    AppDataBase.BASE_EXERCISE_UID + " = ?",
                    new String[]{uid},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                exercise = new BaseExModel(
                        cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.BASE_EXERCISE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.BASE_EXERCISE_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.BASE_EXERCISE_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.BASE_EXERCISE_BODY_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.BASE_EXERCISE_UID))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return exercise;
    }
}