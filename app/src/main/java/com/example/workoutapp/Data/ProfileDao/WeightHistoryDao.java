package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.WEIGHT_HISTORY_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.WEIGHT_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.WEIGHT_MEASUREMENT_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.WEIGHT_VALUE;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class WeightHistoryDao {

    private final SQLiteDatabase db;

    public WeightHistoryDao(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Добавляет запись веса, если значение не пустое и отличается от последнего веса.
     * @param weight объект WeightHistoryModel с новым весом
     * @return id новой записи или -1, если запись не добавлена
     */
    public long addWeightEntry(WeightHistoryModel weight) {
        if (weight == null || weight.getWeightValue() <= 0) {
            return -1;
        }

        WeightHistoryModel latest = getLatestWeight();
        if (latest != null && latest.getWeightValue() == weight.getWeightValue()) {
            return -1; // Вес такой же, не добавляем
        }

        ContentValues values = new ContentValues();
        values.put(WEIGHT_MEASUREMENT_DATE, weight.getMeasurementDate());
        values.put(WEIGHT_VALUE, weight.getWeightValue());

        return db.insert(WEIGHT_HISTORY_TABLE, null, values);
    }

    /**
     * Получение самой последней записи веса
     */
    public WeightHistoryModel getLatestWeight() {
        WeightHistoryModel latestWeight = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
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
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return latestWeight;
    }

    /**
     * Получение всей истории веса
     */
    public List<WeightHistoryModel> getAllWeightHistory() {
        List<WeightHistoryModel> weightList = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
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
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return weightList;
    }

    /**
     * Удаление записи веса по ID
     */
    public void deleteWeightEntryById(long weightId) {
        db.delete(
                WEIGHT_HISTORY_TABLE,
                WEIGHT_ID + " = ?",
                new String[]{String.valueOf(weightId)}
        );
    }
}
