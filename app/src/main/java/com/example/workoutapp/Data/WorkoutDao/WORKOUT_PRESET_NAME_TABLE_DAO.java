package com.example.workoutapp.Data.WorkoutDao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;

import java.util.ArrayList;
import java.util.List;

public class WORKOUT_PRESET_NAME_TABLE_DAO {
    private final AppDataBase dbHelper;
    private final CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingPresetDao;
    private final BASE_EXERCISE_TABLE_DAO baseExerciseDao;


    public WORKOUT_PRESET_NAME_TABLE_DAO(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
        this.connectingPresetDao = new CONNECTING_WORKOUT_PRESET_TABLE_DAO(dbHelper);
        this.baseExerciseDao = new BASE_EXERCISE_TABLE_DAO(dbHelper);
    }

    public long addPresetName(String presetName) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_PRESET_NAME, presetName);
        return database.insert(AppDataBase.WORKOUT_PRESET_NAME_TABLE, null, values);
    }

    public void deletePreset(long presetId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String whereClause = AppDataBase.WORKOUT_PRESET_NAME_ID + " = ?";
        String[] whereArgs = {String.valueOf(presetId)};
        database.delete(AppDataBase.WORKOUT_PRESET_NAME_TABLE, whereClause, whereArgs);
    }

    /**
     * Возвращает список всех пресетов с полными данными об упражнениях.
     * Этот метод выполняет сборку информации из трех таблиц:
     * - `WORKOUT_PRESET_NAME_TABLE` для получения имени пресета.
     * - `CONNECTING_WORKOUT_PRESET_TABLE` для получения ID упражнений, связанных с пресетом.
     * - `BASE_EXERCISE_TABLE` для получения полной информации об упражнениях по их ID.
     *
     * @return Список объектов ExerciseModel, представляющих пресеты.
     */
    public List<ExerciseModel> getAllPresets() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        List<ExerciseModel> presets = new ArrayList<>();
        String[] columns = {
                AppDataBase.WORKOUT_PRESET_NAME_ID,
                AppDataBase.WORKOUT_PRESET_NAME
        };

        try (Cursor cursor = database.query(
                AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                columns,
                null,
                null,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") int idIndex = cursor.getColumnIndex(AppDataBase.WORKOUT_PRESET_NAME_ID);
                @SuppressLint("Range") int nameIndex = cursor.getColumnIndex(AppDataBase.WORKOUT_PRESET_NAME);
                do {
                    long presetId = cursor.getLong(idIndex);
                    String presetName = cursor.getString(nameIndex);

                    // Получаем список ID упражнений для текущего пресета
                    List<Long> baseExIds = connectingPresetDao.getBaseExIdsByPresetId(presetId);

                    // Получаем полную информацию об упражнениях
                    List<Object> exercises = new ArrayList<>();
                    for (Long baseExId : baseExIds) {
                        BaseExModel exercise = baseExerciseDao.getExerciseById(baseExId);
                        if (exercise != null) {
                            exercises.add(exercise);
                        }
                    }

                    // Создаем объект ExerciseModel для пресета
                    ExerciseModel preset = new ExerciseModel(presetId, presetName, exercises);
                    presets.add(preset);
                } while (cursor.moveToNext());
            }
        }
        return presets;
    }

    public void updatePresetName(long presetId, String newName) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_PRESET_NAME, newName);

        String whereClause = AppDataBase.WORKOUT_PRESET_NAME_ID + " = ?";
        String[] whereArgs = {String.valueOf(presetId)};

        database.update(AppDataBase.WORKOUT_PRESET_NAME_TABLE, values, whereClause, whereArgs);
    }
}