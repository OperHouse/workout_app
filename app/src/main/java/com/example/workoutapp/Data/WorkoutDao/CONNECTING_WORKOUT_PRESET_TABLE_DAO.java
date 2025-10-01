package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;

import java.util.ArrayList;
import java.util.List;

public class CONNECTING_WORKOUT_PRESET_TABLE_DAO {
    private final SQLiteDatabase database;


    public CONNECTING_WORKOUT_PRESET_TABLE_DAO(AppDataBase dbHelper) {
        this.database = dbHelper.getReadableDatabase();
    }

    // Метод для добавления связи между пресетом и упражнением
    public long addPresetExercise(long presetId, long baseExId) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.CONNECTING_WORKOUT_PRESET_NAME_ID, presetId);
        values.put(AppDataBase.CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID, baseExId);
        return database.insert(AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE, null, values);
    }

    public List<Long> getBaseExIdsByPresetId(long presetId) {
        List<Long> baseExIds = new ArrayList<>();
        String[] columns = {AppDataBase.CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID};
        String selection = AppDataBase.CONNECTING_WORKOUT_PRESET_NAME_ID + " = ?";
        String[] selectionArgs = {String.valueOf(presetId)};

        try (Cursor cursor = database.query(
                AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int baseExIdIndex = cursor.getColumnIndex(AppDataBase.CONNECTING_WORKOUT_PRESET_BASE_EXERCISE_ID);
                do {
                    long baseExId = cursor.getLong(baseExIdIndex);
                    baseExIds.add(baseExId);
                } while (cursor.moveToNext());
            }
        }
        return baseExIds;
    }

    public void deleteExercisesByPresetId(long presetId) {
        String whereClause = AppDataBase.CONNECTING_WORKOUT_PRESET_NAME_ID + " = ?";
        String[] whereArgs = {String.valueOf(presetId)};
        database.delete(AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE, whereClause, whereArgs);
    }
}
