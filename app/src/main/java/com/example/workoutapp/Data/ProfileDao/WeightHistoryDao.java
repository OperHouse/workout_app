package com.example.workoutapp.Data.ProfileDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;

import java.util.ArrayList;
import java.util.List;

public class WeightHistoryDao {
    private final AppDataBase dbHelper;

    public WeightHistoryDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long addWeightEntry(WeightHistoryModel weight) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AppDataBase.WEIGHT_MEASUREMENT_DATE, weight.getMeasurementDate());
        values.put(AppDataBase.WEIGHT_VALUE, weight.getWeightValue());

        long id = db.insert(AppDataBase.WEIGHT_HISTORY_TABLE, null, values);
        db.close();
        return id;
    }

    // Получение самой последней записи веса
    public WeightHistoryModel getLatestWeight() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        WeightHistoryModel latestWeight = null;

        Cursor cursor = db.query(
                AppDataBase.WEIGHT_HISTORY_TABLE,
                null,
                null,
                null,
                null,
                null,
                AppDataBase.WEIGHT_MEASUREMENT_DATE + " DESC", // Сортируем по убыванию даты
                "1" // Берем только одну запись
        );

        if (cursor != null && cursor.moveToFirst()) {
            latestWeight = new WeightHistoryModel(
                    cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.WEIGHT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WEIGHT_MEASUREMENT_DATE)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(AppDataBase.WEIGHT_VALUE))
            );
            cursor.close();
        }

        db.close();
        return latestWeight;
    }

    public List<WeightHistoryModel> getAllWeightHistory() {
        List<WeightHistoryModel> weightList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                AppDataBase.WEIGHT_HISTORY_TABLE,
                null,
                null,
                null,
                null,
                null,
                AppDataBase.WEIGHT_MEASUREMENT_DATE + " ASC" // Сортируем по возрастанию даты
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                WeightHistoryModel entry = new WeightHistoryModel(
                        cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.WEIGHT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.WEIGHT_MEASUREMENT_DATE)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(AppDataBase.WEIGHT_VALUE))
                );
                weightList.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return weightList;
    }

    public void deleteWeightEntryById(long weightId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(
                AppDataBase.WEIGHT_HISTORY_TABLE,
                AppDataBase.WEIGHT_ID + " = ?",
                new String[]{String.valueOf(weightId)}
        );
        db.close();
    }
}
