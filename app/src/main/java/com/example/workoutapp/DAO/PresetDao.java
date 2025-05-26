package com.example.workoutapp.DAO;

import static com.example.workoutapp.Data.AppDataBase.PRESET_EXERCISE_BODY_TYPE;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EXERCISE_NAME;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EXERCISE_TYPE;
import static com.example.workoutapp.Data.AppDataBase.PRESET_NAME;
import static com.example.workoutapp.Data.AppDataBase.PRESET_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.AppDataBase;
import com.example.workoutapp.Models.ExModel;
import com.example.workoutapp.Models.PresetModel;

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
    public void addPreset(PresetModel preset) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (ExModel exercise : preset.getExercises()) {
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
    public void updatePreset(String oldPresetName, PresetModel updatedPreset) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Удаляем старые записи
        db.delete(PRESET_TABLE, PRESET_NAME + "=?", new String[]{oldPresetName});

        // Вставляем новые
        for (ExModel exercise : updatedPreset.getExercises()) {
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
    public List<PresetModel> getAllPresets() {
        List<PresetModel> presetList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + PRESET_TABLE, null);

        if (cursor.moveToFirst()) {
            Map<String, List<ExModel>> presetMap = new HashMap<>();

            do {
                String presetName = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_NAME));

                ExModel exercise = new ExModel();
                exercise.setExName(cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EXERCISE_NAME)));
                exercise.setExType(cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EXERCISE_TYPE)));
                exercise.setBodyType(cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EXERCISE_BODY_TYPE)));

                if (!presetMap.containsKey(presetName)) {
                    presetMap.put(presetName, new ArrayList<>());
                }
                Objects.requireNonNull(presetMap.get(presetName)).add(exercise);

            } while (cursor.moveToNext());

            // Преобразование в список
            for (Map.Entry<String, List<ExModel>> entry : presetMap.entrySet()) {
                PresetModel preset = new PresetModel(entry.getKey(), entry.getValue());
                presetList.add(preset);
            }

            // Можно отсортировать при необходимости
            presetList.sort((p1, p2) -> p1.getPresetName().compareTo(p2.getPresetName()));
        }

        cursor.close();
        db.close();
        return presetList;
    }
}

