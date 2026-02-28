package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_FOOD_TRACKING_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_FOOD_TRACKING_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.DAILY_FOOD_TRACKING_UID;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_CALORIES;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_CARB;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_FAT;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_FOOD_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.TRACKING_PROTEIN;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DailyFoodTrackingDao {

    private final SQLiteDatabase db;

    public DailyFoodTrackingDao(SQLiteDatabase db) {
        this.db = db;
    }

    // =========================
    // Добавление или обновление записи за день
    // =========================
    public void insertOrUpdate(DailyFoodTrackingModel entry) {
        if (entry == null) return;

        ContentValues values = getContentValues(entry);

        Cursor cursor = null;
        try {
            cursor = db.query(
                    DAILY_FOOD_TRACKING_TABLE,
                    null,
                    DAILY_FOOD_TRACKING_DATE + "=?",
                    new String[]{entry.getDaily_food_tracking_date()},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                db.update(
                        DAILY_FOOD_TRACKING_TABLE,
                        values,
                        DAILY_FOOD_TRACKING_DATE + "=?",
                        new String[]{entry.getDaily_food_tracking_date()}
                );
            } else {
                db.insert(DAILY_FOOD_TRACKING_TABLE, null, values);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    @NonNull
    private static ContentValues getContentValues(DailyFoodTrackingModel entry) {
        ContentValues values = new ContentValues();
        values.put(TRACKING_CALORIES, entry.getDaily_food_tracking_calories());
        values.put(TRACKING_PROTEIN, entry.getDaily_food_tracking_protein());
        values.put(TRACKING_FAT, entry.getDaily_food_tracking_fat());
        values.put(TRACKING_CARB, entry.getDaily_food_tracking_carb());
        values.put(DAILY_FOOD_TRACKING_DATE, entry.getDaily_food_tracking_date());
        values.put(DAILY_FOOD_TRACKING_UID, entry.getDaily_food_tracking_uid());
        return values;
    }

    // =========================
    // Получение записи по дате
    // =========================
    public DailyFoodTrackingModel getFoodTrackingByDate(String date) {
        DailyFoodTrackingModel model = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
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
                        cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_UID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_CALORIES)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_PROTEIN)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_FAT)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_CARB)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_DATE))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return model;
    }

    // =========================
    // Получение последней записи
    // =========================
    public DailyFoodTrackingModel getLatestEntry() {
        DailyFoodTrackingModel model = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
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
                        cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_UID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_CALORIES)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_PROTEIN)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_FAT)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_CARB)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_DATE))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return model;
    }


    public boolean isUidExists(String uid) {
        if (uid == null) return false;
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + DAILY_FOOD_TRACKING_TABLE +
                " WHERE DAILY_FOOD_TRACKING_UID = ?", new String[]{uid});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public void updateUid(int id, String uid) {
        ContentValues cv = new ContentValues();
        cv.put(DAILY_FOOD_TRACKING_UID, uid); // Убедитесь, что имя колонки совпадает с миграцией
        db.update(DAILY_FOOD_TRACKING_TABLE, cv, TRACKING_FOOD_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<DailyFoodTrackingModel> getAllEntries() {
        List<DailyFoodTrackingModel> list = new ArrayList<>();
        Cursor cursor = db.query(DAILY_FOOD_TRACKING_TABLE, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new DailyFoodTrackingModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_FOOD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_UID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_CALORIES)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_PROTEIN)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_FAT)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_CARB)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_DATE))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public DailyFoodTrackingModel getEntryByDate(String date) {
        DailyFoodTrackingModel model = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DAILY_FOOD_TRACKING_TABLE,
                    null,
                    DAILY_FOOD_TRACKING_DATE + " = ?", // Условие поиска по дате
                    new String[]{date},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                model = new DailyFoodTrackingModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_FOOD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_UID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(TRACKING_CALORIES)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_PROTEIN)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_FAT)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(TRACKING_CARB)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DAILY_FOOD_TRACKING_DATE))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return model;
    }
}
