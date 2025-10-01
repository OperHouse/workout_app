package com.example.workoutapp.Data.WorkoutDao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;

import java.util.ArrayList;
import java.util.List;

public class BASE_EXERCISE_TABLE_DAO {
    private final SQLiteDatabase database;


    public BASE_EXERCISE_TABLE_DAO(AppDataBase dbHelper) {
        this.database = dbHelper.getReadableDatabase();
    }

    public long addExercise(BaseExModel exercise) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.BASE_EXERCISE_NAME, exercise.getExName());
        values.put(AppDataBase.BASE_EXERCISE_TYPE, exercise.getExType());
        values.put(AppDataBase.BASE_EXERCISE_BODY_TYPE, exercise.getBodyType());
        // Метод insert возвращает ID (ключ) новой строки
        return database.insert(AppDataBase.BASE_EXERCISE_TABLE, null, values);
    }

    public BaseExModel getExerciseById(long baseExId) {
        BaseExModel exercise = null;
        String[] columns = {
                AppDataBase.BASE_EXERCISE_ID,
                AppDataBase.BASE_EXERCISE_NAME,
                AppDataBase.BASE_EXERCISE_TYPE,
                AppDataBase.BASE_EXERCISE_BODY_TYPE
        };
        String selection = AppDataBase.BASE_EXERCISE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(baseExId)};

        try (Cursor cursor = database.query(
                AppDataBase.BASE_EXERCISE_TABLE,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_NAME));
                @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_TYPE));
                @SuppressLint("Range") String bodyType = cursor.getString(cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_BODY_TYPE));
                exercise = new BaseExModel(id, name, type, bodyType);
            }
        }
        return exercise;
    }

    public List<BaseExModel> getAllExercises() {
        List<BaseExModel> exercises = new ArrayList<>();
        String[] columns = {
                AppDataBase.BASE_EXERCISE_ID,
                AppDataBase.BASE_EXERCISE_NAME,
                AppDataBase.BASE_EXERCISE_TYPE,
                AppDataBase.BASE_EXERCISE_BODY_TYPE
        };

        try (Cursor cursor = database.query(
                AppDataBase.BASE_EXERCISE_TABLE,
                columns,
                null,
                null,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                // Получаем индексы столбцов для более эффективного доступа
                int idIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_ID);
                int nameIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_NAME);
                int typeIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_TYPE);
                int bodyTypeIndex = cursor.getColumnIndex(AppDataBase.BASE_EXERCISE_BODY_TYPE);

                do {
                    long id = cursor.getLong(idIndex);
                    String name = cursor.getString(nameIndex);
                    String type = cursor.getString(typeIndex);
                    String bodyType = cursor.getString(bodyTypeIndex);

                    BaseExModel exercise = new BaseExModel(id, name, type, bodyType);
                    exercises.add(exercise);
                } while (cursor.moveToNext());
            }
        }
        return exercises;
    }

    public void deleteExercise(String exName) {
        String whereClause = AppDataBase.BASE_EXERCISE_NAME + " = ?";
        String[] whereArgs = {exName};
        database.delete(AppDataBase.BASE_EXERCISE_TABLE, whereClause, whereArgs);
    }

    public void updateExercise(String oldName, BaseExModel newExercise) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.BASE_EXERCISE_NAME, newExercise.getExName());
        values.put(AppDataBase.BASE_EXERCISE_TYPE, newExercise.getExType());
        values.put(AppDataBase.BASE_EXERCISE_BODY_TYPE, newExercise.getBodyType());

        String whereClause = AppDataBase.BASE_EXERCISE_NAME + " = ?";
        String[] whereArgs = {oldName};
        database.update(AppDataBase.BASE_EXERCISE_TABLE, values, whereClause, whereArgs);
    }
}
