package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class STRENGTH_SET_DETAILS_TABLE_DAO {

    private final SQLiteDatabase db;

    public STRENGTH_SET_DETAILS_TABLE_DAO(SQLiteDatabase db) {
        this.db = db;
    }

    // =========================
    // Получение всех силовых сетов по ID упражнения
    // =========================
    public List<StrengthSetModel> getSetsForExercise(long exerciseId) {
        List<StrengthSetModel> sets = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT " + AppDataBase.STRENGTH_SET_ID + ", " + AppDataBase.STRENGTH_SET_WEIGHT + ", " + AppDataBase.STRENGTH_SET_REP + ", " + AppDataBase.STRENGTH_SET_STATE + ", " + AppDataBase.STRENGTH_SET_ORDER + " FROM " + AppDataBase.STRENGTH_SET_DETAILS_TABLE + " WHERE " + AppDataBase.STRENGTH_SET_FOREIGN_KEY_EXERCISE + " = ?" + " ORDER BY " + AppDataBase.STRENGTH_SET_ORDER;

            cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_ID);
                int weightIndex = cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_WEIGHT);
                int repIndex = cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_REP);
                int stateIndex = cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_STATE);
                int orderIndex = cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_ORDER);

                do {
                    long id = cursor.getLong(idIndex);
                    double weight = cursor.getDouble(weightIndex);
                    int rep = cursor.getInt(repIndex);
                    String state = cursor.getString(stateIndex);
                    int order = cursor.getInt(orderIndex);

                    sets.add(new StrengthSetModel(id, weight, rep, state, order));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return sets;
    }

    // =========================
    // Добавление нового пустого силового сета
    // =========================
    public void addStrengthSet(long exerciseId) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.STRENGTH_SET_FOREIGN_KEY_EXERCISE, exerciseId);
        values.put(AppDataBase.STRENGTH_SET_WEIGHT, 0.0);
        values.put(AppDataBase.STRENGTH_SET_REP, 0);
        values.put(AppDataBase.STRENGTH_SET_STATE, "unfinished");
        values.put(AppDataBase.STRENGTH_SET_ORDER, 0); // TODO: вычислить следующий порядковый номер

        db.insert(AppDataBase.STRENGTH_SET_DETAILS_TABLE, null, values);
    }

    // =========================
    // Получение последнего силового сета
    // =========================
    public StrengthSetModel getLastStrengthSet(long exerciseId) {
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + AppDataBase.STRENGTH_SET_DETAILS_TABLE + " WHERE " + AppDataBase.STRENGTH_SET_FOREIGN_KEY_EXERCISE + " = ?" + " ORDER BY " + AppDataBase.STRENGTH_SET_ID + " DESC LIMIT 1";

            cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});

            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_ID));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_WEIGHT));
                int rep = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_REP));
                String state = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_STATE));
                int order = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_ORDER));

                return new StrengthSetModel(id, weight, rep, state, order);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    // =========================
    // Обновление силового сета
    // =========================
    public void updateStrengthSet(StrengthSetModel set) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.STRENGTH_SET_WEIGHT, set.getStrength_set_weight());
        values.put(AppDataBase.STRENGTH_SET_REP, set.getStrength_set_rep());
        values.put(AppDataBase.STRENGTH_SET_STATE, set.getStrength_set_state());

        db.update(AppDataBase.STRENGTH_SET_DETAILS_TABLE, values, AppDataBase.STRENGTH_SET_ID + " = ?", new String[]{String.valueOf(set.getStrength_set_id())});
    }

    // =========================
    // Удаление силового сета
    // =========================
    public void deleteStrengthSet(StrengthSetModel set) {
        if (set == null) return;
        db.delete(AppDataBase.STRENGTH_SET_DETAILS_TABLE, AppDataBase.STRENGTH_SET_ID + " = ?", new String[]{String.valueOf(set.getStrength_set_id())});
    }

    // =========================
    // Удаление всех силовых сетов для упражнения
    // =========================
    public void deleteSetsForExercise(long exerciseId) {
        db.delete(AppDataBase.STRENGTH_SET_DETAILS_TABLE, AppDataBase.STRENGTH_SET_FOREIGN_KEY_EXERCISE + " = ?", new String[]{String.valueOf(exerciseId)});
    }

    // =========================
    // Обновление порядка (order) силового сета
    // =========================
    public void updateSetOrder(StrengthSetModel set) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.STRENGTH_SET_ORDER, set.getStrength_set_order());

        db.update(AppDataBase.STRENGTH_SET_DETAILS_TABLE, values, AppDataBase.STRENGTH_SET_ID + " = ?", new String[]{String.valueOf(set.getStrength_set_id())});
    }

    // =========================
    // Добавление силового сета с параметрами (для синхронизации из облака)
    // =========================
    public void addStrengthSet(long exerciseId, double weight, int rep, int order, String state) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.STRENGTH_SET_FOREIGN_KEY_EXERCISE, exerciseId);
        values.put(AppDataBase.STRENGTH_SET_WEIGHT, weight);
        values.put(AppDataBase.STRENGTH_SET_REP, rep);
        values.put(AppDataBase.STRENGTH_SET_STATE, state);
        values.put(AppDataBase.STRENGTH_SET_ORDER, order);

        db.insert(AppDataBase.STRENGTH_SET_DETAILS_TABLE, null, values);
    }
}
