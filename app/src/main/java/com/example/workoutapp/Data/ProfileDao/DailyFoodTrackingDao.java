package com.example.workoutapp.Data.ProfileDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;

import java.util.ArrayList;
import java.util.List;

public class DailyFoodTrackingDao {
    private final AppDataBase dbHelper;

    public DailyFoodTrackingDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление новой записи о питании
    public long insertFoodTracking(DailyFoodTrackingModel foodEntry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppDataBase.DAILY_FOOD_TRACKING_DATE, foodEntry.getDailyFoodTrackingDate());
        values.put(AppDataBase.TRACKING_CALORIES, foodEntry.getTrackingCalories());
        values.put(AppDataBase.TRACKING_PROTEIN, foodEntry.getTrackingProtein());
        values.put(AppDataBase.TRACKING_FAT, foodEntry.getTrackingFat());
        values.put(AppDataBase.TRACKING_CARB, foodEntry.getTrackingCarb());

        long id = db.insert(AppDataBase.DAILY_FOOD_TRACKING_TABLE, null, values);
        db.close();
        return id;
    }

    public void updateFoodTracking(DailyFoodTrackingModel foodEntry) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppDataBase.DAILY_FOOD_TRACKING_DATE, foodEntry.getDailyFoodTrackingDate());
        values.put(AppDataBase.TRACKING_CALORIES, foodEntry.getTrackingCalories());
        values.put(AppDataBase.TRACKING_PROTEIN, foodEntry.getTrackingProtein());
        values.put(AppDataBase.TRACKING_FAT, foodEntry.getTrackingFat());
        values.put(AppDataBase.TRACKING_CARB, foodEntry.getTrackingCarb());

        db.update(
                AppDataBase.DAILY_FOOD_TRACKING_TABLE,
                values,
                AppDataBase.TRACKING_FOOD_ID + " = ?",
                new String[]{String.valueOf(foodEntry.getTrackingFoodId())}
        );

        db.close();
    }

    public DailyFoodTrackingModel getFoodTrackingByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        DailyFoodTrackingModel foodEntry = null;

        Cursor cursor = db.query(
                AppDataBase.DAILY_FOOD_TRACKING_TABLE,
                null,
                AppDataBase.DAILY_FOOD_TRACKING_DATE + " = ?",
                new String[]{date},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            foodEntry = new DailyFoodTrackingModel(
                    cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.TRACKING_FOOD_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.DAILY_FOOD_TRACKING_DATE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.TRACKING_CALORIES)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.TRACKING_PROTEIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.TRACKING_FAT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.TRACKING_CARB))
            );
            cursor.close();
        }

        db.close();
        return foodEntry;
    }
}
