package com.example.workoutapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataBase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DataBase";
    private static final int DATABASE_VERSION = 1;

    //=========================ExData============================//

    public static final String TABLE_EXERCISE = "exercise_table";
    public  static final String EXERCISE_NAME = "exercise_name";
    public static final String EXERCISE_TYPE = "exercise_type";
    public  static final String BODY_TYPE = "body_type";

    //=========================PresetsData=======================//

    public static final String TABLE_PRESETS = "presets_table";
    public  static final String PRESET_NAME = "preset_name";
    public  static final String EXERCISE_NAME2 = "exercise_name2";
    public static final String EXERCISE_TYPE2 = "exercise_type2";
    public  static final String BODY_TYPE2 = "body_type2";
    public DataBase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createExerciseTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_EXERCISE + " (" +  EXERCISE_NAME
                + " TEXT, " + EXERCISE_TYPE + " TEXT, " + BODY_TYPE + " TEXT)";
        String createPresetsTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_PRESETS + " ("
                + PRESET_NAME + " TEXT, "
                + EXERCISE_NAME2 + " TEXT, "   // правильное имя столбца для имени упражнения
                + EXERCISE_TYPE2 + " TEXT, "
                + BODY_TYPE2 + " TEXT)";
        db.execSQL(createExerciseTableQuery);
        db.execSQL(createPresetsTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESETS);
        onCreate(db);
    }

    // Function to add a new exercise
    public void addExercise(ExModel exercise) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EXERCISE_NAME, exercise.getExName());
        values.put(EXERCISE_TYPE, exercise.getExType());
        values.put(BODY_TYPE, exercise.getBodyType());

        db.insert(TABLE_EXERCISE, null, values);
        db.close();
    }

    // Function to delete an exercise by name
    public void deleteExercise(String exerciseName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXERCISE, EXERCISE_NAME + "=?", new String[]{exerciseName});
        db.close();
    }

    // Function to change an existing exercise by name
    public void changeExercise(String oldName, ExModel updatedExercise) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EXERCISE_NAME, updatedExercise.getExName());
        values.put(EXERCISE_TYPE, updatedExercise.getExType());
        values.put(BODY_TYPE, updatedExercise.getBodyType());

        db.update(TABLE_EXERCISE, values, EXERCISE_NAME + "=?", new String[]{oldName});
        db.close();
    }

    // Function to get all exercises
    @SuppressLint("Range")
    public List<ExModel> getAllExercise() {
        List<ExModel> exerciseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_EXERCISE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    ExModel exercise = new ExModel();
                    exercise.setExName(cursor.getString(cursor.getColumnIndex(EXERCISE_NAME)));
                    exercise.setExType(cursor.getString(cursor.getColumnIndex(EXERCISE_TYPE)));
                    exercise.setBodyType(cursor.getString(cursor.getColumnIndex(BODY_TYPE)));
                    exerciseList.add(exercise);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return exerciseList;
    }
    //======================================PresetsFun==========================//

    public void addPreset(PresetModel preset) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Для каждого упражнения в пресете добавляем его в базу данных
        for (ExModel exercise : preset.getExercises()) {
            ContentValues values = new ContentValues();
            values.put(PRESET_NAME, preset.getPresetName());
            values.put(EXERCISE_NAME2, exercise.getExName());
            values.put(EXERCISE_TYPE2, exercise.getExType());
            values.put(BODY_TYPE2, exercise.getBodyType());

            db.insert(TABLE_PRESETS, null, values);
        }
        db.close();
    }


    public void deletePreset(String presetName, String exerciseName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Удаляем конкретное упражнение по имени пресета и имени упражнения
        db.delete(TABLE_PRESETS, PRESET_NAME + "=? AND " + EXERCISE_NAME2 + "=?", new String[]{presetName, exerciseName});
        db.close();
    }

    // Function to change an existing preset by preset name
    public void changePreset(String oldPresetName, PresetModel updatedPreset) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Удаляем все старые упражнения, связанные с этим пресетом
        db.delete(TABLE_PRESETS, PRESET_NAME + "=?", new String[]{oldPresetName});
        Log.d("ChangePreset", "Old exercises deleted for preset: " + oldPresetName);

        // Добавляем новые упражнения для обновленного пресета
        for (ExModel exercise : updatedPreset.getExercises()) {
            ContentValues values = new ContentValues();
            values.put(PRESET_NAME, updatedPreset.getPresetName());
            values.put(EXERCISE_NAME2, exercise.getExName());
            values.put(EXERCISE_TYPE2, exercise.getExType());
            values.put(BODY_TYPE2, exercise.getBodyType());

            // Печать значений, чтобы увидеть, что передается в запрос
            Log.d("ChangePreset", "Inserting new exercise: " + exercise.getExName());

            // Вставляем новое упражнение
            db.insert(TABLE_PRESETS, null, values);
        }

        db.close();
    }

    // Function to get all presets
    @SuppressLint("Range")
    public List<PresetModel> getAllPresets() {
        List<PresetModel> presetList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_PRESETS;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null) {
            // Создаем карту для группировки упражнений по имени пресета
            Map<String, List<ExModel>> presetMap = new HashMap<>();

            if (cursor.moveToFirst()) {
                do {
                    // Получаем имя пресета
                    String presetName = cursor.getString(cursor.getColumnIndex(PRESET_NAME));

                    // Создаем новый объект ExModel для каждого упражнения
                    ExModel exercise = new ExModel();
                    exercise.setExName(cursor.getString(cursor.getColumnIndex(EXERCISE_NAME2)));
                    exercise.setExType(cursor.getString(cursor.getColumnIndex(EXERCISE_TYPE2)));
                    exercise.setBodyType(cursor.getString(cursor.getColumnIndex(BODY_TYPE2)));

                    // Добавляем упражнение в список по имени пресета
                    if (!presetMap.containsKey(presetName)) {
                        presetMap.put(presetName, new ArrayList<>());
                    }
                    Objects.requireNonNull(presetMap.get(presetName)).add(exercise);
                } while (cursor.moveToNext());
            }
            cursor.close();

            // Преобразуем карту в список объектов PresetModel
            for (Map.Entry<String, List<ExModel>> entry : presetMap.entrySet()) {
                PresetModel preset = new PresetModel(entry.getKey(), entry.getValue());
                presetList.add(preset);
            }

            // Сортируем список пресетов по имени
            presetList.sort(new Comparator<PresetModel>() {
                @Override
                public int compare(PresetModel p1, PresetModel p2) {
                    return p1.getPresetName().compareTo(p2.getPresetName());
                }
            });
        }

        db.close();
        return presetList;
    }

    //=====================================Check_Data============================================//
    public void printAllPresets() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRESETS, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String presetName = cursor.getString(cursor.getColumnIndex(PRESET_NAME));
                    @SuppressLint("Range") String exerciseName = cursor.getString(cursor.getColumnIndex(EXERCISE_NAME2));
                    @SuppressLint("Range") String exerciseType = cursor.getString(cursor.getColumnIndex(EXERCISE_TYPE2));
                    @SuppressLint("Range") String bodyType = cursor.getString(cursor.getColumnIndex(BODY_TYPE2));

                    Log.d("DB_LOG", "Preset: Name = " + presetName + ", Exercise = " + exerciseName + ", Type = " + exerciseType + ", Body Type = " + bodyType);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
    }
    public void printAllExercises() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EXERCISE, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String exerciseName = cursor.getString(cursor.getColumnIndex(EXERCISE_NAME));
                    @SuppressLint("Range") String exerciseType = cursor.getString(cursor.getColumnIndex(EXERCISE_TYPE));
                    @SuppressLint("Range") String bodyType = cursor.getString(cursor.getColumnIndex(BODY_TYPE));

                    Log.d("DB_LOG", "Exercise: Name = " + exerciseName + ", Type = " + exerciseType + ", Body Type = " + bodyType);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
    }
}
