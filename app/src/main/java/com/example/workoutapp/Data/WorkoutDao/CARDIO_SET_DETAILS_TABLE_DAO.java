package com.example.workoutapp.Data.WorkoutDao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;

import java.util.ArrayList;
import java.util.List;

public class CARDIO_SET_DETAILS_TABLE_DAO {

    private final SQLiteDatabase database;

    public CARDIO_SET_DETAILS_TABLE_DAO(AppDataBase dbHelper) {
        this.database = dbHelper.getReadableDatabase();
    }

    /**
     * Возвращает список кардио-сетов для конкретного упражнения.
     * @param exerciseId ID упражнения, для которого нужно получить сеты.
     * @return Список объектов CardioSetModel.
     */
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
}