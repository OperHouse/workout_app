package com.example.workoutapp.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;

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
            values.put(AppDataBase.CONNECTING_MEAL_EAT_ID, foodId);
            db.insert(AppDataBase.CONNECTING_MEAL_TABLE, null, values);
        }

        db.close();
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
                int foodId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.CONNECTING_MEAL_EAT_ID));

                Log.d("ConnectingMealDao", "Meal ID: " + mealId + " ↔ Food ID: " + foodId);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
    }

}
