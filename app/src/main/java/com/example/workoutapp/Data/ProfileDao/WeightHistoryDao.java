package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.WEIGHT_HISTORY_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.WEIGHT_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.WEIGHT_MEASUREMENT_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.WEIGHT_VALUE;

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

    /**
     * Добавляет запись веса, если значение не пустое и отличается от последнего веса.
     * @param weight объект WeightHistoryModel с новым весом
     * @return id новой записи или -1, если запись не добавлена
     */
    public long addWeightEntry(WeightHistoryModel weight) {
        if (weight == null || weight.getWeightValue() <= 0) {
            // Вес не указан или <= 0 — ничего не добавляем
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Проверяем последний вес
        WeightHistoryModel latest = getLatestWeight(db);
        if (latest != null && latest.getWeightValue() == weight.getWeightValue()) {
            db.close();
            return -1; // Вес такой же, не добавляем
        }

        ContentValues values = new ContentValues();
        values.put(WEIGHT_MEASUREMENT_DATE, weight.getMeasurementDate());
        values.put(WEIGHT_VALUE, weight.getWeightValue());

        long id = db.insert(WEIGHT_HISTORY_TABLE, null, values);
        db.close();
        return id;
    }

    // Получение самой последней записи веса
    public WeightHistoryModel getLatestWeight() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        WeightHistoryModel latest = getLatestWeight(db);
        db.close();
        return latest;
    }

    // Вспомогательный метод, чтобы не открывать и закрывать БД дважды
    private WeightHistoryModel getLatestWeight(SQLiteDatabase db) {
        WeightHistoryModel latestWeight = null;

        Cursor cursor = db.query(
                WEIGHT_HISTORY_TABLE,
                null,
                null,
                null,
                null,
                null,
                WEIGHT_MEASUREMENT_DATE + " DESC",
                "1"
        );

        if (cursor != null && cursor.moveToFirst()) {
            latestWeight = new WeightHistoryModel(
                    cursor.getLong(cursor.getColumnIndexOrThrow(WEIGHT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(WEIGHT_MEASUREMENT_DATE)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(WEIGHT_VALUE))
            );
            cursor.close();
        }

        return latestWeight;
    }

    public List<WeightHistoryModel> getAllWeightHistory() {
        List<WeightHistoryModel> weightList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                WEIGHT_HISTORY_TABLE,
                null,
                null,
                null,
                null,
                null,
                WEIGHT_MEASUREMENT_DATE + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                WeightHistoryModel entry = new WeightHistoryModel(
                        cursor.getLong(cursor.getColumnIndexOrThrow(WEIGHT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(WEIGHT_MEASUREMENT_DATE)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(WEIGHT_VALUE))
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
                WEIGHT_HISTORY_TABLE,
                WEIGHT_ID + " = ?",
                new String[]{String.valueOf(weightId)}
        );
        db.close();
    }
}
