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
        if (weight == null || weight.getWeight_history_value() <= 0) return -1;

        ContentValues values = new ContentValues();
        // Мы НЕ вставляем WEIGHT_ID, SQLite инкрементирует его сама
        values.put(WEIGHT_UID, weight.getWeight_history_uid());
        values.put(WEIGHT_MEASUREMENT_DATE, weight.getWeight_history_measurementDate());
        values.put(WEIGHT_VALUE, weight.getWeight_history_value());

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
        model.setWeight_history_id(cursor.getLong(cursor.getColumnIndexOrThrow(WEIGHT_ID)));
        model.setWeight_history_uid(cursor.getString(cursor.getColumnIndexOrThrow(WEIGHT_UID)));
        model.setWeight_history_measurementDate(cursor.getString(cursor.getColumnIndexOrThrow(WEIGHT_MEASUREMENT_DATE)));
        model.setWeight_history_value(cursor.getFloat(cursor.getColumnIndexOrThrow(WEIGHT_VALUE)));
        return model;
    }

    public WeightHistoryModel getWeightByUid(String uid) {
        if (uid == null) return null;
        WeightHistoryModel weight = null;

        try (Cursor cursor = db.query(WEIGHT_HISTORY_TABLE, null,
                WEIGHT_UID + " = ?", new String[]{uid},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                weight = mapCursorToModel(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weight;
    }
}