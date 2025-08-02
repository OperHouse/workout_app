package com.example.workoutapp.Data.WorkoutDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.PRESET_EXERCISE_BODY_TYPE;
import static com.example.workoutapp.Data.Tables.AppDataBase.PRESET_EXERCISE_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.PRESET_EXERCISE_TYPE;
import static com.example.workoutapp.Data.Tables.AppDataBase.PRESET_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.PRESET_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PresetDao {
    private final AppDataBase dbHelper;

    public PresetDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление нового пресета
    public void addPreset(ExerciseModel preset) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (BaseExModel exercise : preset.getExercises()) {
            ContentValues values = new ContentValues();
            values.put(PRESET_NAME, preset.getPresetName());
            values.put(PRESET_EXERCISE_NAME, exercise.getExName());
            values.put(PRESET_EXERCISE_TYPE, exercise.getExType());
            values.put(PRESET_EXERCISE_BODY_TYPE, exercise.getBodyType());

            db.insert(PRESET_TABLE, null, values);
        }

        db.close();
    }

    // Удаление одного упражнения из пресета
    public void deletePreset(String presetName, String exerciseName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PRESET_TABLE, PRESET_NAME + "=? AND " + PRESET_EXERCISE_NAME + "=?", new String[]{presetName, exerciseName});
        db.close();
    }

    // Полное обновление пресета
    public void updatePreset(String oldPresetName, ExerciseModel updatedPreset) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Удаляем старые записи
        db.delete(PRESET_TABLE, PRESET_NAME + "=?", new String[]{oldPresetName});

        // Вставляем новые
        for (BaseExModel exercise : updatedPreset.getExercises()) {
            ContentValues values = new ContentValues();
            values.put(PRESET_NAME, updatedPreset.getPresetName());
            values.put(PRESET_EXERCISE_NAME, exercise.getExName());
            values.put(PRESET_EXERCISE_TYPE, exercise.getExType());
            values.put(PRESET_EXERCISE_BODY_TYPE, exercise.getBodyType());

            db.insert(PRESET_TABLE, null, values);
        }

        db.close();
    }

    // Получение всех пресетов
    public List<ExerciseModel> getAllPresets() {
        List<ExerciseModel> presetList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + PRESET_TABLE, null);

        if (cursor.moveToFirst()) {
            Map<String, List<BaseExModel>> presetMap = new HashMap<>();

            do {
                String presetName = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_NAME));

                BaseExModel exercise = new BaseExModel();
                exercise.setExName(cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EXERCISE_NAME)));
                exercise.setExType(cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EXERCISE_TYPE)));
                exercise.setBodyType(cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EXERCISE_BODY_TYPE)));

                if (!presetMap.containsKey(presetName)) {
                    presetMap.put(presetName, new ArrayList<>());
                }
                Objects.requireNonNull(presetMap.get(presetName)).add(exercise);

            } while (cursor.moveToNext());

            // Преобразование в список
            for (Map.Entry<String, List<BaseExModel>> entry : presetMap.entrySet()) {
                ExerciseModel preset = new ExerciseModel(entry.getKey(), entry.getValue());
                presetList.add(preset);
            }

            // Можно отсортировать при необходимости
            presetList.sort((p1, p2) -> p1.getPresetName().compareTo(p2.getPresetName()));
        }

        cursor.close();
        db.close();
        return presetList;
    }

    public void logAllPresets() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + PRESET_TABLE, null);

        if (cursor.moveToFirst()) {
            do {
                String presetName = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_NAME));
                String exerciseName = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EXERCISE_NAME));
                String exerciseType = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EXERCISE_TYPE));
                String bodyType = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EXERCISE_BODY_TYPE));

                Log.d("PRESET_LOG", "Preset: " + presetName +
                        ", Exercise: " + exerciseName +
                        ", Type: " + exerciseType +
                        ", BodyPart: " + bodyType);
            } while (cursor.moveToNext());
        } else {
            Log.d("PRESET_LOG", "Preset table is empty.");
        }

        cursor.close();
        db.close();
    }
}

