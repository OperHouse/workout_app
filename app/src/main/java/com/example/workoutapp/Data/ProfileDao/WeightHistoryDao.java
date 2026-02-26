package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.*;
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

    public long addWeightEntry(WeightHistoryModel weight) {
        if (weight == null || weight.getWeightValue() <= 0) return -1;

        ContentValues values = new ContentValues();
        // Мы НЕ вставляем WEIGHT_ID, SQLite инкрементирует его сама
        values.put(WEIGHT_UID, weight.getWeightUid());
        values.put(WEIGHT_MEASUREMENT_DATE, weight.getMeasurementDate());
        values.put(WEIGHT_VALUE, weight.getWeightValue());

        // Если запись с таким UID уже есть, ничего не делаем (избегаем дублей)
        return db.insertWithOnConflict(WEIGHT_HISTORY_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public boolean isWeightUidExists(String uid) {
        if (uid == null) return false;
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + WEIGHT_HISTORY_TABLE + " WHERE " + WEIGHT_UID + " = ?", new String[]{uid});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public WeightHistoryModel getLatestWeight() {
        WeightHistoryModel latestWeight = null;
        try (Cursor cursor = db.query(WEIGHT_HISTORY_TABLE, null, null, null, null, null,
                WEIGHT_MEASUREMENT_DATE + " DESC, " + WEIGHT_ID + " DESC", "1")) {
            if (cursor != null && cursor.moveToFirst()) {
                latestWeight = mapCursorToModel(cursor);
            }
        }
        return latestWeight;
    }

    public List<WeightHistoryModel> getAllWeightHistory() {
        List<WeightHistoryModel> weightList = new ArrayList<>();
        try (Cursor cursor = db.query(WEIGHT_HISTORY_TABLE, null, null, null, null, null, WEIGHT_MEASUREMENT_DATE + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    weightList.add(mapCursorToModel(cursor));
                } while (cursor.moveToNext());
            }
        }
        return weightList;
    }

    private WeightHistoryModel mapCursorToModel(Cursor cursor) {
        WeightHistoryModel model = new WeightHistoryModel();
        model.setWeightId(cursor.getLong(cursor.getColumnIndexOrThrow(WEIGHT_ID)));
        model.setWeightUid(cursor.getString(cursor.getColumnIndexOrThrow(WEIGHT_UID)));
        model.setMeasurementDate(cursor.getString(cursor.getColumnIndexOrThrow(WEIGHT_MEASUREMENT_DATE)));
        model.setWeightValue(cursor.getFloat(cursor.getColumnIndexOrThrow(WEIGHT_VALUE)));
        return model;
    }
}