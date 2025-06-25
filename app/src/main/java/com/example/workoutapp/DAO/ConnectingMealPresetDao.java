package com.example.workoutapp.DAO;

import static com.example.workoutapp.Data.AppDataBase.CONNECTING_MEAL_PRESET_EAT_ID;
import static com.example.workoutapp.Data.AppDataBase.CONNECTING_MEAL_PRESET_NAME_ID;
import static com.example.workoutapp.Data.AppDataBase.CONNECTING_MEAL_PRESET_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;

import java.util.ArrayList;
import java.util.List;

public class ConnectingMealPresetDao {
    private final AppDataBase dbHelper;

    public ConnectingMealPresetDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void addMealPresetConnection(int mealNameId, int presetEatId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CONNECTING_MEAL_PRESET_NAME_ID, mealNameId);
        values.put(CONNECTING_MEAL_PRESET_EAT_ID, presetEatId);
        db.insert(CONNECTING_MEAL_PRESET_TABLE, null, values);
        db.close();
    }

    public List<Integer> getEatIdsForPreset(int presetId) {
        List<Integer> eatIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + CONNECTING_MEAL_PRESET_EAT_ID + " FROM " + CONNECTING_MEAL_PRESET_TABLE + " WHERE " + CONNECTING_MEAL_PRESET_NAME_ID + " = ?",
                new String[]{String.valueOf(presetId)}
        );

        if (cursor.moveToFirst()) {
            do {
                int eatId = cursor.getInt(cursor.getColumnIndexOrThrow(CONNECTING_MEAL_PRESET_EAT_ID));
                eatIds.add(eatId);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return eatIds;
    }

    public void deleteAllForPreset(int presetId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(CONNECTING_MEAL_PRESET_TABLE, CONNECTING_MEAL_PRESET_NAME_ID + " = ?", new String[]{String.valueOf(presetId)});
        db.close();
    }
    public boolean doesEatIdExist(int eatId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + CONNECTING_MEAL_PRESET_TABLE +
                        " WHERE " + CONNECTING_MEAL_PRESET_EAT_ID + " = ? LIMIT 1",
                new String[]{String.valueOf(eatId)}
        );

        boolean exists = cursor.moveToFirst(); // если есть хотя бы одна строка — true

        cursor.close();
        db.close();

        return exists;
    }
    public void logAllMealPresetConnections() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                CONNECTING_MEAL_PRESET_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int mealNameId = cursor.getInt(cursor.getColumnIndexOrThrow(CONNECTING_MEAL_PRESET_NAME_ID));
                int eatId = cursor.getInt(cursor.getColumnIndexOrThrow(CONNECTING_MEAL_PRESET_EAT_ID));

                Log.d("MealPresetConnection", "Meal Name ID: " + mealNameId + ", Eat ID: " + eatId);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Log.d("MealPresetConnection", "No connections found.");
        }

        db.close();
    }
}
