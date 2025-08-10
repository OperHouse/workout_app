package com.example.workoutapp.Data.WorkoutDao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;

import java.util.ArrayList;
import java.util.List;

public class STRENGTH_SET_DETAILS_TABLE_DAO {

    private final SQLiteDatabase database;

    public STRENGTH_SET_DETAILS_TABLE_DAO(AppDataBase dbHelper) {
        this.database = dbHelper.getReadableDatabase();
    }

    /**
     * Возвращает список силовых сетов для конкретного упражнения.
     * @param exerciseId ID упражнения, для которого нужно получить сеты.
     * @return Список объектов StrengthSetModel.
     */
    public List<StrengthSetModel> getSetsForExercise(long exerciseId) {
        List<StrengthSetModel> strengthSets = new ArrayList<>();
        Cursor cursor = null;

        try {
            String query = "SELECT " +
                    AppDataBase.STRENGTH_SET_ID + ", " +
                    AppDataBase.STRENGTH_SET_WEIGHT + ", " +
                    AppDataBase.STRENGTH_SET_REP + ", " +
                    AppDataBase.STRENGTH_SET_STATE + ", " +
                    AppDataBase.STRENGTH_SET_ORDER +
                    " FROM " + AppDataBase.STRENGTH_SET_DETAILS_TABLE +
                    " WHERE " + AppDataBase.STRENGTH_SET_FOREIGN_KEY_EXERCISE + " = ?" +
                    " ORDER BY " + AppDataBase.STRENGTH_SET_ORDER;

            cursor = database.rawQuery(query, new String[]{String.valueOf(exerciseId)});

            if (cursor != null && cursor.moveToFirst()) {
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

                    StrengthSetModel set = new StrengthSetModel(id, weight, rep, state, order);
                    strengthSets.add(set);

                } while (cursor.moveToNext());
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return strengthSets;
    }
}