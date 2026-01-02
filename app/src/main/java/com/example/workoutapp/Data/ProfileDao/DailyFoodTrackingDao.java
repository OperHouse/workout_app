package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_FOOD_TRACKING_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_FOOD_TRACKING_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_CALORIES;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_CARB;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_FAT;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_FOOD_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_PROTEIN;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;

public class DailyFoodTrackingDao {

    private final AppDataBase dbHelper;

    public DailyFoodTrackingDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление записи о питании за день
    public void insertOrUpdate(DailyFoodTrackingModel entry) {
        if (entry == null) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRACKING_CALORIES, entry.getCalories());
        values.put(TRACKING_PROTEIN, entry.getProtein());
        values.put(TRACKING_FAT, entry.getFat());
        values.put(TRACKING_CARB, entry.getCarb());
        values.put(DAILY_FOOD_TRACKING_DATE, entry.getDate());

        // Проверяем — есть ли запись на эту дату
        Cursor cursor = db.query(
                DAILY_FOOD_TRACKING_TABLE,
                null,
                DAILY_FOOD_TRACKING_DATE + "=?",
                new String[]{entry.getDate()},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            db.update(
                    DAILY_FOOD_TRACKING_TABLE,
                    values,
                    DAILY_FOOD_TRACKING_DATE + "=?",
                    new String[]{entry.getDate()}
            );
            cursor.close();
        } else {
            db.insert(DAILY_FOOD_TRACKING_TABLE, null, values);
        }
    }

    // Получить данные за конкретную дату
    public DailyFoodTrackingModel getFoodTrackingByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        DailyFoodTrackingModel model = null;

        Cursor cursor = db.query(
                DAILY_FOOD_TRACKING_TABLE,
                null,
                DAILY_FOOD_TRACKING_DATE + "=?",
                new String[]{date},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            model = new DailyFoodTrackingModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_FOOD_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_CALORIES)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_PROTEIN)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_FAT)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_CARB)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_DATE))
            );
            cursor.close();
        }
        return model;
    }
    public DailyFoodTrackingModel getLatestEntry() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        DailyFoodTrackingModel model = null;

        // Сортируем по дате в обратном порядке и берем 1 запись
        Cursor cursor = db.query(
                DAILY_FOOD_TRACKING_TABLE,
                null,
                null,
                null,
                null,
                null,
                DAILY_FOOD_TRACKING_DATE + " DESC",
                "1"
        );

        if (cursor != null && cursor.moveToFirst()) {
            model = new DailyFoodTrackingModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_FOOD_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_CALORIES)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_PROTEIN)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_FAT)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_CARB)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_DATE))
            );
            cursor.close();
        }
        return model;
    }

}
