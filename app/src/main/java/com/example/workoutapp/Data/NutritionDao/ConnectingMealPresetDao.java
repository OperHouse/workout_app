package com.example.workoutapp.Data.NutritionDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.CONNECTING_MEAL_PRESET_FOOD_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.CONNECTING_MEAL_PRESET_NAME_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.CONNECTING_MEAL_PRESET_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

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

    /**
     * Получает список eatId для конкретного пресета
     */
    public List<Integer> getEatIdsForPreset(int presetId) {
        List<Integer> eatIds = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    CONNECTING_MEAL_PRESET_TABLE,
                    new String[]{CONNECTING_MEAL_PRESET_FOOD_ID},
                    CONNECTING_MEAL_PRESET_NAME_ID + " = ?",
                    new String[]{String.valueOf(presetId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int eatId = cursor.getInt(cursor.getColumnIndexOrThrow(CONNECTING_MEAL_PRESET_FOOD_ID));
                    eatIds.add(eatId);
                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) cursor.close();
        }

        return eatIds;
    }

    // ========================= DELETE CONNECTIONS ========================= //

    /**
     * Удаляет все связи для конкретного пресета
     */
    public void deleteAllForPreset(long presetId) {
        db.delete(
                CONNECTING_MEAL_PRESET_TABLE,
                CONNECTING_MEAL_PRESET_NAME_ID + " = ?",
                new String[]{String.valueOf(presetId)}
        );
        Log.d("ConnectingMealPresetDao", "Deleted all connections for presetId: " + presetId);
    }

    // ========================= CHECK EXISTENCE ========================= //

    /**
     * Проверяет, существует ли хотя бы одна связь с данным eatId
     */
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
    public void deleteAll() {
        db.delete(CONNECTING_MEAL_PRESET_TABLE, null, null);
    }
}
