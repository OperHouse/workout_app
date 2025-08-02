package com.example.workoutapp.Data.WorkoutDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.EXERCISE_BODY_TYPE;
import static com.example.workoutapp.Data.Tables.AppDataBase.EXERCISE_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.EXERCISE_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.EXERCISE_TYPE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;

import java.util.ArrayList;
import java.util.List;

public class ExerciseDao {

    private final AppDataBase dbHelper;

    public ExerciseDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление нового упражнения
    public void addExercise(BaseExModel exercise) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EXERCISE_NAME, exercise.getExName());
        values.put(EXERCISE_TYPE, exercise.getExType());
        values.put(EXERCISE_BODY_TYPE, exercise.getBodyType());

        db.insert(EXERCISE_TABLE, null, values);
        db.close();
    }

    // Удаление упражнения по имени
    public void deleteExercise(String exerciseName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(EXERCISE_TABLE, EXERCISE_NAME + "=?", new String[]{exerciseName});
        db.close();
    }

    // Обновление упражнения по имени
    public void updateExercise(String oldName, BaseExModel updatedExercise) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EXERCISE_NAME, updatedExercise.getExName());
        values.put(EXERCISE_TYPE, updatedExercise.getExType());
        values.put(EXERCISE_BODY_TYPE, updatedExercise.getBodyType());

        db.update(EXERCISE_TABLE, values, EXERCISE_NAME + "=?", new String[]{oldName});
        db.close();
    }

    // Получение всех упражнений
    public List<BaseExModel> getAllExercises() {
        List<BaseExModel> exerciseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + EXERCISE_TABLE, null);

        if (cursor.moveToFirst()) {
            do {
                BaseExModel exercise = new BaseExModel();
                exercise.setExName(cursor.getString(cursor.getColumnIndexOrThrow(EXERCISE_NAME)));
                exercise.setExType(cursor.getString(cursor.getColumnIndexOrThrow(EXERCISE_TYPE)));
                exercise.setBodyType(cursor.getString(cursor.getColumnIndexOrThrow(EXERCISE_BODY_TYPE)));
                exerciseList.add(exercise);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return exerciseList;
    }
}