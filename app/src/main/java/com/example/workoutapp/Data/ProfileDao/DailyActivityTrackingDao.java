package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_ACTIVITY_TRACKING_ACTIVITY_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_ACTIVITY_TRACKING_ACTIVITY_STEPS;
import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_ACTIVITY_TRACKING_CALORIES_BURN;
import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_ACTIVITY_TRACKING_TABLE;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;

import net.sqlcipher.database.SQLiteDatabase;

public class DailyActivityTrackingDao {

    private final SQLiteDatabase db;

    public DailyActivityTrackingDao(SQLiteDatabase db) {
        this.db = db;
    }

    // Добавление или обновление дневных данных активности
    public void insertOrUpdate(DailyActivityTrackingModel model) {

        Cursor cursor = db.query(
                DAILY_ACTIVITY_TRACKING_TABLE,
                null,
                DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE + " = ?",
                new String[]{model.getDate()},
                null, null, null
        );

        ContentValues values = new ContentValues();
        values.put(DAILY_ACTIVITY_TRACKING_ACTIVITY_STEPS, model.getTrackingActivitySteps());
        values.put(DAILY_ACTIVITY_TRACKING_CALORIES_BURN, model.getTrackingCaloriesBurned());
        values.put(DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE, model.getDate());

        if (cursor.moveToFirst()) {
            db.update(
                    DAILY_ACTIVITY_TRACKING_TABLE,
                    values,
                    DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE + " = ?",
                    new String[]{model.getDate()}
            );
        } else {
            db.insert(DAILY_ACTIVITY_TRACKING_TABLE, null, values);
        }

        cursor.close();
    }

    // Получение данных по дате
    public DailyActivityTrackingModel getActivityByDate(String date) {

        Cursor cursor = db.query(
                DAILY_ACTIVITY_TRACKING_TABLE,
                null,
                DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE + " = ?",
                new String[]{date},
                null, null, null
        );

        DailyActivityTrackingModel model = null;
        if (cursor.moveToFirst()) {
            model = new DailyActivityTrackingModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DAILY_ACTIVITY_TRACKING_ACTIVITY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DAILY_ACTIVITY_TRACKING_ACTIVITY_STEPS)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(DAILY_ACTIVITY_TRACKING_CALORIES_BURN))
            );
        }

        cursor.close();
        return model;
    }

    // Получение последней активности
    public DailyActivityTrackingModel getLatestActivity() {

        Cursor cursor = db.query(
                DAILY_ACTIVITY_TRACKING_TABLE,
                null,
                null, null, null, null,
                DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE + " DESC",
                "1"
        );

        DailyActivityTrackingModel model = null;
        if (cursor.moveToFirst()) {
            model = new DailyActivityTrackingModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DAILY_ACTIVITY_TRACKING_ACTIVITY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DAILY_ACTIVITY_TRACKING_ACTIVITY_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DAILY_ACTIVITY_TRACKING_ACTIVITY_STEPS)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(DAILY_ACTIVITY_TRACKING_CALORIES_BURN))
            );
        }

        cursor.close();
        return model;
    }
}
