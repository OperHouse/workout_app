package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Data.Tables.AppDataBase;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class CONNECTING_WORKOUT_PRESET_TABLE_DAO {

    private final SQLiteDatabase db;

    public CONNECTING_WORKOUT_PRESET_TABLE_DAO(SQLiteDatabase db) {
        this.db = db;
    }

    // =========================
    // Добавление связи между пресетом и базовым упражнением
    // =========================
    public long addPresetExercise(long presetId, long baseExId) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.CONNECTING_WORKOUT_PRESET_NAME_ID, presetId);
        values.put(AppDataBase.CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID, baseExId);

        return db.insert(AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE, null, values);
    }

    // =========================
    // Получение всех базовых упражнений по ID пресета
    // =========================
    public List<Long> getBaseExIdsByPresetId(long presetId) {
        List<Long> baseExIds = new ArrayList<>();
        String[] columns = {AppDataBase.CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID};
        String selection = AppDataBase.CONNECTING_WORKOUT_PRESET_NAME_ID + " = ?";
        String[] selectionArgs = {String.valueOf(presetId)};

        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                int baseExIdIndex = cursor.getColumnIndexOrThrow(AppDataBase.CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID);
                do {
                    baseExIds.add(cursor.getLong(baseExIdIndex));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return baseExIds;
    }

    // =========================
    // Удаление всех связей по ID пресета
    // =========================
    public void deleteExercisesByPresetId(long presetId) {
        String whereClause = AppDataBase.CONNECTING_WORKOUT_PRESET_NAME_ID + " = ?";
        String[] whereArgs = {String.valueOf(presetId)};
        db.delete(AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE, whereClause, whereArgs);
    }

    public void deleteAll() {
        db.delete(AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE, null, null);
    }
}
