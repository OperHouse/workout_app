package com.example.workoutapp.Data.WorkoutDao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;

import java.util.ArrayList;
import java.util.List;

public class CARDIO_SET_DETAILS_TABLE_DAO {

    private final AppDataBase dbHelper;

    public CARDIO_SET_DETAILS_TABLE_DAO(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Возвращает список кардио-сетов для конкретного упражнения.
     * @param exerciseId ID упражнения, для которого нужно получить сеты.
     * @return Список объектов CardioSetModel.
     */
    public List<CardioSetModel> getSetsForExercise(long exerciseId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
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

            cursor = database.rawQuery(query, new String[]{String.valueOf(exerciseId)});

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_ID);
                int tempIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_TEMP);
                int timeIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_TIME);
                int distanceIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_DISTANCE);
                int stateIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_STATE);
                int orderIndex = cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_ORDER);

                do {
                    long id = cursor.getLong(idIndex);
                    double temp = cursor.getDouble(tempIndex);
                    int time = cursor.getInt(timeIndex);
                    double distance = cursor.getDouble(distanceIndex);
                    String state = cursor.getString(stateIndex);
                    int order = cursor.getInt(orderIndex);

                    CardioSetModel set = new CardioSetModel(id, temp, time, distance, state, order);
                    cardioSets.add(set);

                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return cardioSets;
    }

    /**
     * Добавляет новый пустой кардио-сет в базу данных для указанного упражнения.
     * @param exerciseId ID упражнения, к которому относится сет.
     */
    public void addCardioSet(long exerciseId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.CARDIO_SET_FOREIGN_KEY_EXERCISE, exerciseId);
        values.put(AppDataBase.CARDIO_SET_TEMP, 0.0);
        values.put(AppDataBase.CARDIO_SET_TIME, 0);
        values.put(AppDataBase.CARDIO_SET_DISTANCE, 0.0);
        values.put(AppDataBase.CARDIO_SET_STATE, "unfinished");
        values.put(AppDataBase.CARDIO_SET_ORDER, 0); // TODO: Здесь нужно будет получить следующий порядковый номер.

        database.insert(AppDataBase.CARDIO_SET_DETAILS_TABLE, null, values);
    }

    /**
     * Возвращает последний добавленный кардио-сет для конкретного упражнения из базы данных.
     * @param exerciseId ID упражнения.
     * @return Объект CardioSetModel или null, если сет не найден.
     */
    public CardioSetModel getLastCardioSet(long exerciseId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + AppDataBase.CARDIO_SET_DETAILS_TABLE +
                    " WHERE " + AppDataBase.CARDIO_SET_FOREIGN_KEY_EXERCISE + " = ?" +
                    " ORDER BY " + AppDataBase.CARDIO_SET_ID + " DESC LIMIT 1";

            cursor = database.rawQuery(query, new String[]{String.valueOf(exerciseId)});

            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_ID));
                double temp = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_TEMP));
                int time = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_TIME));
                double distance = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_DISTANCE));
                String state = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_STATE));
                int order = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.CARDIO_SET_ORDER));

                return new CardioSetModel(id, temp, time, distance, state, order);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Обновляет данные кардио-сета в базе данных.
     * @param set Объект CardioSetModel с обновленными данными.
     */
    public void updateCardioSet(CardioSetModel set) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.CARDIO_SET_TEMP, set.getTemp());
        values.put(AppDataBase.CARDIO_SET_TIME, set.getTime());
        values.put(AppDataBase.CARDIO_SET_DISTANCE, set.getDistance());
        values.put(AppDataBase.CARDIO_SET_STATE, set.getState());

        database.update(AppDataBase.CARDIO_SET_DETAILS_TABLE,
                values,
                AppDataBase.CARDIO_SET_ID + " = ?",
                new String[]{String.valueOf(set.getCardio_set_id())});
    }

    /**
     * Удаляет кардио-сет из базы данных.
     * @param set Объект CardioSetModel, который нужно удалить.
     */
    public void deleteCardioSet(CardioSetModel set) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        if (set == null) {
            return;
        }
        database.delete(AppDataBase.CARDIO_SET_DETAILS_TABLE,
                AppDataBase.CARDIO_SET_ID + " = ?",
                new String[]{String.valueOf(set.getCardio_set_id())});
    }


    /**
     * Удаляет все кардио-подходы, связанные с определенным упражнением.
     * @param exerciseId ID упражнения, чьи подходы нужно удалить.
     */
    public void deleteSetsForExercise(long exerciseId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(AppDataBase.CARDIO_SET_DETAILS_TABLE,
                AppDataBase.CARDIO_SET_FOREIGN_KEY_EXERCISE + " = ?",
                new String[]{String.valueOf(exerciseId)});
    }
    /**
     * Обновляет порядок (order) у кардио-сета.
     * @param set Объект CardioSetModel с новым порядком.
     */
    public void updateSetOrder(CardioSetModel set) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.CARDIO_SET_ORDER, set.getOrder());

        database.update(
                AppDataBase.CARDIO_SET_DETAILS_TABLE,
                values,
                AppDataBase.CARDIO_SET_ID + " = ?",
                new String[]{String.valueOf(set.getCardio_set_id())}
        );
    }
}