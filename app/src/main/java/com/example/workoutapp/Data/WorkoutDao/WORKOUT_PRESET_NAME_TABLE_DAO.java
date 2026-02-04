package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class WORKOUT_PRESET_NAME_TABLE_DAO {

    private final SQLiteDatabase db;
    private final CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingPresetDao;
    private final BASE_EXERCISE_TABLE_DAO baseExerciseDao;

    public WORKOUT_PRESET_NAME_TABLE_DAO(SQLiteDatabase db) {
        this.db = db;
        this.connectingPresetDao = new CONNECTING_WORKOUT_PRESET_TABLE_DAO(db);
        this.baseExerciseDao = new BASE_EXERCISE_TABLE_DAO(db);
    }

    // =========================
    // Добавление нового пресета
    // =========================
    public long addPreset(String presetName) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_PRESET_NAME, presetName);
        return db.insert(AppDataBase.WORKOUT_PRESET_NAME_TABLE, null, values);
    }

    // =========================
    // Удаление пресета по ID
    // =========================
    public void deletePreset(long presetId) {
        db.delete(
                AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                AppDataBase.WORKOUT_PRESET_NAME_ID + " = ?",
                new String[]{String.valueOf(presetId)}
        );
        // Также удаляем связанные упражнения
        connectingPresetDao.deleteExercisesByPresetId(presetId);
    }

    // =========================
    // Получение всех пресетов с полными данными об упражнениях
    // =========================
    public List<ExerciseModel> getAllPresets() {
        List<ExerciseModel> presets = new ArrayList<>();
        Cursor cursor = null;

        try {
            String[] columns = {
                    AppDataBase.WORKOUT_PRESET_NAME_ID,
                    AppDataBase.WORKOUT_PRESET_NAME
            };

            cursor = db.query(
                    AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_PRESET_NAME_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(AppDataBase.WORKOUT_PRESET_NAME);

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
                    presets.add(new ExerciseModel(presetId, presetName, exercises));

                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) cursor.close();
        }

        return presets;
    }

    // =========================
    // Обновление имени пресета
    // =========================
    public void updatePresetName(long presetId, String newName) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.WORKOUT_PRESET_NAME, newName);

        db.update(
                AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                values,
                AppDataBase.WORKOUT_PRESET_NAME_ID + " = ?",
                new String[]{String.valueOf(presetId)}
        );
    }
}
