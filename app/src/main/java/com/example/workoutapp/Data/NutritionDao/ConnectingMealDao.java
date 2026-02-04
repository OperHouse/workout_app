package com.example.workoutapp.Data.NutritionDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.CONNECTING_MEAL_FOOD_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.CONNECTING_MEAL_NAME_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.CONNECTING_MEAL_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ConnectingMealDao {

    private final SQLiteDatabase db;

    public ConnectingMealDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ========================= ADD CONNECTIONS ========================= //

    // Подключение нескольких продуктов к одному приёму пищи
    public void connecting(long mealId, List<Long> foodIds) {
        for (Long foodId : foodIds) {
            connectingSingleFood(mealId, foodId);
        }
    }

    // Подключение одного продукта к приёму пищи
    public void connectingSingleFood(long mealId, long foodId) {
        ContentValues values = new ContentValues();
        values.put(CONNECTING_MEAL_NAME_ID, mealId);
        values.put(CONNECTING_MEAL_FOOD_ID, foodId);
        db.insert(CONNECTING_MEAL_TABLE, null, values);
        Log.d("ConnectingMealDao", "Connected foodId " + foodId + " to mealId " + mealId);
    }

    // ========================= GET CONNECTIONS ========================= //

    // Получаем список foodId для конкретного mealId
    public List<Long> getFoodIdsForMeal(long mealId) {
        List<Long> foodIds = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    CONNECTING_MEAL_TABLE,
                    new String[]{CONNECTING_MEAL_FOOD_ID},
                    CONNECTING_MEAL_NAME_ID + " = ?",
                    new String[]{String.valueOf(mealId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long foodId = cursor.getLong(cursor.getColumnIndexOrThrow(CONNECTING_MEAL_FOOD_ID));
                    foodIds.add(foodId);
                    Log.d("ConnectingMealDao", "Found foodId: " + foodId + " for mealId: " + mealId);
                } while (cursor.moveToNext());
            } else {
                Log.d("ConnectingMealDao", "No foodIds found for mealId: " + mealId);
            }

        } finally {
            if (cursor != null) cursor.close();
        }

        return foodIds;
    }

    // ========================= DELETE CONNECTIONS ========================= //

    // Удаление конкретной связи еды и приёма пищи
    public void deleteConnection(long mealId, long foodId) {
        db.delete(
                CONNECTING_MEAL_TABLE,
                CONNECTING_MEAL_NAME_ID + " = ? AND " + CONNECTING_MEAL_FOOD_ID + " = ?",
                new String[]{String.valueOf(mealId), String.valueOf(foodId)}
        );
        Log.d("ConnectingMealDao", "Deleted connection: mealId=" + mealId + ", foodId=" + foodId);
    }

    // Удаление всех связей для конкретного приёма пищи
    public void deleteAllConnectionsForMeal(long mealId) {
        db.delete(
                CONNECTING_MEAL_TABLE,
                CONNECTING_MEAL_NAME_ID + " = ?",
                new String[]{String.valueOf(mealId)}
        );
        Log.d("ConnectingMealDao", "Deleted all connections for mealId: " + mealId);
    }
}
