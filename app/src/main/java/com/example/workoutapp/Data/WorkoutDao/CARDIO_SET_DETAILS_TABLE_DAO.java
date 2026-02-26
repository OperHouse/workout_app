package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class CARDIO_SET_DETAILS_TABLE_DAO {

    private final SQLiteDatabase db;

    public CARDIO_SET_DETAILS_TABLE_DAO(SQLiteDatabase db) {
        this.db = db;
    }

    // =========================
    // Получение всех кардио-сетов для упражнения
    // =========================
    public List<CardioSetModel> getSetsForExercise(long exerciseId) {
        List<CardioSetModel> cardioSets = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT " +
                    AppDataBase.CARDIO_SET_ID + ", " +
                    AppDataBase.CARDIO_SET_TEMP + ", " +
                    AppDataBase.CARDIO_SET_TIME + ", " +
                    AppDataBase.CARDIO_SET_DISTANCE + ", " +
                    AppDataBase.CARDIO_SET_STATE + ", " +
                    AppDataBase.CARDIO_SET_ORDER +
                    " FROM " + AppDataBase.CARDIO_SET_DETAILS_TABLE +
                    " WHERE " + AppDataBase.CARDIO_SET_FOREIGN_KEY_EXERCISE + " = ?" +
                    " ORDER BY " + AppDataBase.CARDIO_SET_ORDER;

            cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_ID);
                int tempIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_TEMP);
                int timeIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_TIME);
                int distanceIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_DISTANCE);
                int stateIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_STATE);
                int orderIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_ORDER);

                do {
                    CardioSetModel set = new CardioSetModel(
                            cursor.getLong(idIndex),
                            cursor.getDouble(tempIndex),
                            cursor.getInt(timeIndex),
                            cursor.getDouble(distanceIndex),
                            cursor.getString(stateIndex),
                            cursor.getInt(orderIndex)
                    );
                    cardioSets.add(set);
                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) cursor.close();
        }

        return cardioSets;
    }

    // =========================
    // Добавление нового кардио-сета
    // =========================
    public void addCardioSet(long exerciseId) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.CARDIO_SET_FOREIGN_KEY_EXERCISE, exerciseId);
        values.put(AppDataBase.CARDIO_SET_TEMP, 0.0);
        values.put(AppDataBase.CARDIO_SET_TIME, 0);
        values.put(AppDataBase.CARDIO_SET_DISTANCE, 0.0);
        values.put(AppDataBase.CARDIO_SET_STATE, "unfinished");
        values.put(AppDataBase.CARDIO_SET_ORDER, 0); // TODO: Здесь нужно будет получить следующий порядковый номер.

        db.insert(AppDataBase.CARDIO_SET_DETAILS_TABLE, null, values);
    }

    // =========================
    // Получение последнего кардио-сета
    // =========================
    public CardioSetModel getLastCardioSet(long exerciseId) {
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + AppDataBase.CARDIO_SET_DETAILS_TABLE +
                    " WHERE " + AppDataBase.CARDIO_SET_FOREIGN_KEY_EXERCISE + " = ?" +
                    " ORDER BY " + AppDataBase.CARDIO_SET_ID + " DESC LIMIT 1";

            cursor = db.rawQuery(query, new String[]{String.valueOf(exerciseId)});

            if (cursor.moveToFirst()) {
                return new CardioSetModel(
                        cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_ID)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_TEMP)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_TIME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_DISTANCE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_STATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_ORDER))
                );
            }

        } finally {
            if (cursor != null) cursor.close();
        }

        return null;
    }

    // =========================
    // Обновление кардио-сета
    // =========================
    public void updateCardioSet(CardioSetModel set) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.CARDIO_SET_TEMP, set.getCardio_set_temp());
        values.put(AppDataBase.CARDIO_SET_TIME, set.getCardio_set_time());
        values.put(AppDataBase.CARDIO_SET_DISTANCE, set.getCardio_set_distance());
        values.put(AppDataBase.CARDIO_SET_STATE, set.getCardio_set_state());
        values.put(AppDataBase.CARDIO_SET_ORDER, set.getCardio_set_order());

        db.update(
                AppDataBase.CARDIO_SET_DETAILS_TABLE,
                values,
                AppDataBase.CARDIO_SET_ID + " = ?",
                new String[]{String.valueOf(set.getCardio_set_id())}
        );
    }

    // =========================
    // Удаление кардио-сета
    // =========================
    public void deleteCardioSet(CardioSetModel set) {
        if (set == null) return;

        db.delete(
                AppDataBase.CARDIO_SET_DETAILS_TABLE,
                AppDataBase.CARDIO_SET_ID + " = ?",
                new String[]{String.valueOf(set.getCardio_set_id())}
        );
    }

    // =========================
    // Удаление всех сетов упражнения
    // =========================
    public void deleteSetsForExercise(long exerciseId) {
        db.delete(
                AppDataBase.CARDIO_SET_DETAILS_TABLE,
                AppDataBase.CARDIO_SET_FOREIGN_KEY_EXERCISE + " = ?",
                new String[]{String.valueOf(exerciseId)}
        );
    }

    // =========================
    // Обновление порядка кардио-сета
    // =========================
    public void updateSetOrder(CardioSetModel set) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.CARDIO_SET_ORDER, set.getCardio_set_order());

        db.update(
                AppDataBase.CARDIO_SET_DETAILS_TABLE,
                values,
                AppDataBase.CARDIO_SET_ID + " = ?",
                new String[]{String.valueOf(set.getCardio_set_id())}
        );
    }

    // =========================
    // Добавление кардио-сета с параметрами (для синхронизации из облака)
    // =========================
    public void addCardioSet(long exerciseId, double temp, int time, double distance, int order, String state) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.CARDIO_SET_FOREIGN_KEY_EXERCISE, exerciseId);
        values.put(AppDataBase.CARDIO_SET_TEMP, temp);
        values.put(AppDataBase.CARDIO_SET_TIME, time);
        values.put(AppDataBase.CARDIO_SET_DISTANCE, distance);
        values.put(AppDataBase.CARDIO_SET_ORDER, order);
        values.put(AppDataBase.CARDIO_SET_STATE, state);

        db.insert(AppDataBase.CARDIO_SET_DETAILS_TABLE, null, values);
    }
}
