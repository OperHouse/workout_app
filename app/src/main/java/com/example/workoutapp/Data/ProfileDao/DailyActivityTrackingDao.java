package com.example.workoutapp.Data.ProfileDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;

import java.util.ArrayList;
import java.util.List;

public class DailyActivityTrackingDao {
    private final AppDataBase dbHelper;

    public DailyActivityTrackingDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление или обновление ежедневной активности
    public long insertActivity(DailyActivityTrackingModel activity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppDataBase.TRACKING_ACTIVITY_DATE, activity.getTrackingActivityDate());
        values.put(AppDataBase.TRACKING_ACTIVITY_STEPS, activity.getTrackingActivitySteps());
        values.put(AppDataBase.DAILY_ACTIVITY_TRACKING_CALORIES, activity.getDailyActivityTrackingCalories());

        long id = db.insert(AppDataBase.DAILY_ACTIVITY_TRACKING_TABLE, null, values);
        db.close();
        return id;
    }

    public void updateActivity(DailyActivityTrackingModel activity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppDataBase.TRACKING_ACTIVITY_DATE, activity.getTrackingActivityDate());
        values.put(AppDataBase.TRACKING_ACTIVITY_STEPS, activity.getTrackingActivitySteps());
        values.put(AppDataBase.DAILY_ACTIVITY_TRACKING_CALORIES, activity.getDailyActivityTrackingCalories());

        db.update(
                AppDataBase.DAILY_ACTIVITY_TRACKING_TABLE,
                values,
                AppDataBase.TRACKING_ACTIVITY_ID + " = ?",
                new String[]{String.valueOf(activity.getTrackingActivityId())}
        );
        db.close();
    }

    public DailyActivityTrackingModel getActivityByDate(String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        DailyActivityTrackingModel activity = null;

        Cursor cursor = db.query(
                AppDataBase.DAILY_ACTIVITY_TRACKING_TABLE,
                null,
                AppDataBase.TRACKING_ACTIVITY_DATE + " = ?",
                new String[]{date},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            activity = new DailyActivityTrackingModel(
                    cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.TRACKING_ACTIVITY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.TRACKING_ACTIVITY_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.TRACKING_ACTIVITY_STEPS)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.DAILY_ACTIVITY_TRACKING_CALORIES))
            );
            cursor.close();
        }

        db.close();
        return activity;
    }
}
