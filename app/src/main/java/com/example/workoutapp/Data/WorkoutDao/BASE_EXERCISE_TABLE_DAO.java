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

    // DAO получает ГОТОВУЮ открытую БД
    public BASE_EXERCISE_TABLE_DAO(SQLiteDatabase db) {
        this.db = db;
    }

    // =========================
    // Добавление упражнения
    // =========================
    public long addExercise(BaseExModel exercise) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.BASE_EXERCISE_NAME, exercise.getExName());
        values.put(AppDataBase.BASE_EXERCISE_TYPE, exercise.getExType());
        values.put(AppDataBase.BASE_EXERCISE_BODY_TYPE, exercise.getBodyType());

        return db.insert(BASE_EXERCISE_TABLE, null, values);
    }

    // =========================
    // Получение упражнения по ID
    // =========================
    public BaseExModel getExerciseById(long baseExId) {
        BaseExModel exercise = null;
        Cursor cursor = null;

        try {
            String[] columns = {
                    AppDataBase.BASE_EXERCISE_ID,
                    AppDataBase.BASE_EXERCISE_NAME,
                    AppDataBase.BASE_EXERCISE_TYPE,
                    AppDataBase.BASE_EXERCISE_BODY_TYPE
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

                exercise = new BaseExModel(id, name, type, bodyType);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return exercise;
    }

    // =========================
    // Получение всех упражнений
    // =========================
    public List<BaseExModel> getAllExercises() {
        List<BaseExModel> exercises = new ArrayList<>();
        Cursor cursor = null;

        try {
            String[] columns = {
                    AppDataBase.BASE_EXERCISE_ID,
                    AppDataBase.BASE_EXERCISE_NAME,
                    AppDataBase.BASE_EXERCISE_TYPE,
                    AppDataBase.BASE_EXERCISE_BODY_TYPE
            };

            cursor = db.query(
                    BASE_EXERCISE_TABLE,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            int idIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_ID);
            int nameIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_NAME);
            int typeIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_TYPE);
            int bodyTypeIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_BODY_TYPE);

            while (cursor.moveToNext()) {
                BaseExModel exercise = new BaseExModel(
                        cursor.getLong(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getString(typeIndex),
                        cursor.getString(bodyTypeIndex)
                );
                exercises.add(exercise);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return exercises;
    }

    // =========================
    // Удаление упражнения
    // =========================
    public void deleteExercise(long id) {
        db.delete(
                BASE_EXERCISE_TABLE,
                AppDataBase.BASE_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // =========================
    // Обновление упражнения
    // =========================
    public void updateExercise(BaseExModel exercise) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.BASE_EXERCISE_NAME, exercise.getExName());
        values.put(AppDataBase.BASE_EXERCISE_TYPE, exercise.getExType());
        values.put(AppDataBase.BASE_EXERCISE_BODY_TYPE, exercise.getBodyType());

        db.update(
                BASE_EXERCISE_TABLE,
                values,
                AppDataBase.BASE_EXERCISE_ID + " = ?",
                new String[]{String.valueOf(exercise.getBase_ex_id())}
        );
    }


    public long getCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + BASE_EXERCISE_TABLE, null);
        long count = 0;
        if (cursor.moveToFirst()) count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    public void deleteAllExercises() {
        db.delete(AppDataBase.BASE_EXERCISE_TABLE, null, null);
    }
}
