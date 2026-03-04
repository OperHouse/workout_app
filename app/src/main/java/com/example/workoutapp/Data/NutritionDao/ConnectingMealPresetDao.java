package com.example.workoutapp.Data.NutritionDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.CONNECTING_MEAL_PRESET_FOOD_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.CONNECTING_MEAL_PRESET_NAME_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.CONNECTING_MEAL_PRESET_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ConnectingMealPresetDao {

    private final SQLiteDatabase db;

    public ConnectingMealPresetDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ========================= ADD CONNECTION ========================= //

    /**
     * Добавляет связь между пресетом и едой
     */
    public void addMealPresetConnection(long mealNameId, long presetFoodId) {
        ContentValues values = new ContentValues();
        values.put(CONNECTING_MEAL_PRESET_NAME_ID, mealNameId);
        values.put(CONNECTING_MEAL_PRESET_FOOD_ID, presetFoodId);
        db.insert(CONNECTING_MEAL_PRESET_TABLE, null, values);
        Log.d("ConnectingMealPresetDao", "Connected presetFoodId " + presetFoodId + " to mealNameId " + mealNameId);
    }

    // ========================= GET CONNECTIONS ========================= //

    public List<Integer> getEatIdsForPreset(long presetId) {
        List<Integer> eatIds = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    CONNECTING_MEAL_PRESET_TABLE,
                    new String[]{CONNECTING_MEAL_PRESET_FOOD_ID},
                    CONNECTING_MEAL_PRESET_NAME_ID + " = ?",
                    new String[]{String.valueOf(presetId)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    eatIds.add(cursor.getInt(cursor.getColumnIndexOrThrow(CONNECTING_MEAL_PRESET_FOOD_ID)));
                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) cursor.close();
        }

        return eatIds;
    }

    // ========================= DELETE CONNECTIONS ========================= //

    public void deleteAllForPreset(long presetId) {
        db.delete(
                CONNECTING_MEAL_PRESET_TABLE,
                CONNECTING_MEAL_PRESET_NAME_ID + " = ?",
                new String[]{String.valueOf(presetId)}
        );
        Log.d("ConnectingMealPresetDao", "Deleted all connections for presetId: " + presetId);
    }

    public void deleteAll() {
        db.delete(CONNECTING_MEAL_PRESET_TABLE, null, null);
    }

    public void deleteConnectionsByMealUid(String mealUid) {
        long mealId = getMealIdByUid(mealUid);
        if (mealId == -1) return;

        db.delete(
                CONNECTING_MEAL_PRESET_TABLE,
                CONNECTING_MEAL_PRESET_NAME_ID + " = ?",
                new String[]{String.valueOf(mealId)}
        );
    }

    // ========================= CHECK EXISTENCE ========================= //

    public boolean doesEatIdExist(long eatId) {
        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = db.query(
                    CONNECTING_MEAL_PRESET_TABLE,
                    new String[]{"1"},
                    CONNECTING_MEAL_PRESET_FOOD_ID + " = ?",
                    new String[]{String.valueOf(eatId)},
                    null,
                    null,
                    "1"
            );
            exists = cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
        }

        return exists;
    }

    // ========================= HELPERS ========================= //

    private long getMealIdByUid(String mealUid) {
        Cursor cursor = db.query(
                AppDataBase.MEAL_PRESET_NAME_TABLE,
                new String[]{AppDataBase.MEAL_PRESET_NAME_ID},
                AppDataBase.MEAL_PRESET_UID + " = ?",
                new String[]{mealUid},
                null, null, null
        );

        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        return id;
    }
}