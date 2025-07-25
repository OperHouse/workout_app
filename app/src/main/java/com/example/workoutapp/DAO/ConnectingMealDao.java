package com.example.workoutapp.DAO;


import static com.example.workoutapp.Data.AppDataBase.CONNECTING_MEAL_FOOD_ID;
import static com.example.workoutapp.Data.AppDataBase.CONNECTING_MEAL_NAME_ID;
import static com.example.workoutapp.Data.AppDataBase.CONNECTING_MEAL_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;

import java.util.ArrayList;
import java.util.List;

public class ConnectingMealDao {
    private final AppDataBase dbHelper;
    public ConnectingMealDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void connecting(long mealId, List<Long> foodIds) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (Long foodId : foodIds) {
            ContentValues values = new ContentValues();
            values.put(AppDataBase.CONNECTING_MEAL_NAME_ID, mealId);
            values.put(AppDataBase.CONNECTING_MEAL_FOOD_ID, foodId);
            db.insert(AppDataBase.CONNECTING_MEAL_TABLE, null, values);
        }

        db.close();
    }


    public List<Long> getFoodIdsForMeal(int mealId) {
        List<Long> eatIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Запрос на получение всех eatId для конкретного mealId
        Cursor cursor = db.rawQuery(
                "SELECT " + CONNECTING_MEAL_FOOD_ID + " FROM " + CONNECTING_MEAL_TABLE +
                        " WHERE " + CONNECTING_MEAL_NAME_ID + " = ?",
                new String[]{String.valueOf(mealId)}
        );

        Log.d("ConnectingMealDao", "Executing query for mealId: " + mealId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Используем getLong для получения значения типа long
                long eatId = cursor.getLong(cursor.getColumnIndexOrThrow(CONNECTING_MEAL_FOOD_ID));
                Log.d("ConnectingMealDao", "Found eatId: " + eatId);
                eatIds.add(eatId); // Добавляем найденный eatId в список
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Log.d("ConnectingMealDao", "No eatIds found for mealId: " + mealId);
        }

        db.close();

        return eatIds;
    }

    //==============================Логирование======================================//
    public void logAllConnections() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                AppDataBase.CONNECTING_MEAL_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int mealId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.CONNECTING_MEAL_NAME_ID));
                int foodId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.CONNECTING_MEAL_FOOD_ID));

                Log.d("ConnectingMealDao", "Meal ID: " + mealId + " ↔ Food ID: " + foodId);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
    }
    public int countConnections() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + CONNECTING_MEAL_TABLE, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        Log.d("ConnectingMealDao", "📊 Кол-во связей в таблице: " + count);
        return count;
    }

}
