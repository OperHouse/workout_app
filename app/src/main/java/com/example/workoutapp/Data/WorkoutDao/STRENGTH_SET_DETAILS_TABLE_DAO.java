package com.example.workoutapp.Data.WorkoutDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;

import java.util.ArrayList;
import java.util.List;

public class STRENGTH_SET_DETAILS_TABLE_DAO {

    private final AppDataBase dbHelper;

    public STRENGTH_SET_DETAILS_TABLE_DAO(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Возвращает список силовых сетов для конкретного упражнения.
     * @param exerciseId ID упражнения, для которого нужно получить сеты.
     * @return Список объектов StrengthSetModel.
     */
    public List<StrengthSetModel> getSetsForExercise(long exerciseId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
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

    /**
     * Добавляет новый пустой силовой сет в базу данных для указанного упражнения.
     * @param exerciseId ID упражнения, к которому относится сет.
     */
    public void addStrengthSet(long exerciseId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.STRENGTH_SET_FOREIGN_KEY_EXERCISE, exerciseId);
        values.put(AppDataBase.STRENGTH_SET_WEIGHT, 0.0);
        values.put(AppDataBase.STRENGTH_SET_REP, 0);
        values.put(AppDataBase.STRENGTH_SET_STATE, "unfinished");
        values.put(AppDataBase.STRENGTH_SET_ORDER, 0); // TODO: Здесь нужно будет получить следующий порядковый номер.

        database.insert(AppDataBase.STRENGTH_SET_DETAILS_TABLE, null, values);
    }

    public StrengthSetModel getLastStrengthSet(long exerciseId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + AppDataBase.STRENGTH_SET_DETAILS_TABLE +
                    " WHERE " + AppDataBase.STRENGTH_SET_FOREIGN_KEY_EXERCISE + " = ?" +
                    " ORDER BY " + AppDataBase.STRENGTH_SET_ID + " DESC LIMIT 1";

            cursor = database.rawQuery(query, new String[]{String.valueOf(exerciseId)});

            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_ID));
                double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_WEIGHT));
                int rep = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_REP));
                String state = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_STATE));
                int order = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.STRENGTH_SET_ORDER));

                return new StrengthSetModel(id, weight, rep, state, order);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Обновляет данные силового сета в базе данных.
     * @param set Объект StrengthSetModel с обновленными данными.
     */
    public void updateStrengthSet(StrengthSetModel set) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.STRENGTH_SET_WEIGHT, set.getWeight());
        values.put(AppDataBase.STRENGTH_SET_REP, set.getRep());
        values.put(AppDataBase.STRENGTH_SET_STATE, set.getState());

        database.update(AppDataBase.STRENGTH_SET_DETAILS_TABLE,
                values,
                AppDataBase.STRENGTH_SET_ID + " = ?",
                new String[]{String.valueOf(set.getStrength_set_id())});
    }

    /**
     * Удаляет силовой сет из базы данных.
     * @param set Объект StrengthSetModel, который нужно удалить.
     */
    public void deleteStrengthSet(StrengthSetModel set) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        if (set == null) {
            return;
        }
        database.delete(AppDataBase.STRENGTH_SET_DETAILS_TABLE,
                AppDataBase.STRENGTH_SET_ID + " = ?",
                new String[]{String.valueOf(set.getStrength_set_id())});
    }

    /**
     * Удаляет все силовые подходы, связанные с определенным упражнением.
     * @param exerciseId ID упражнения, чьи подходы нужно удалить.
     */
    public void deleteSetsForExercise(long exerciseId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(AppDataBase.STRENGTH_SET_DETAILS_TABLE,
                AppDataBase.STRENGTH_SET_FOREIGN_KEY_EXERCISE + " = ?",
                new String[]{String.valueOf(exerciseId)});
    }

    /**
     * Обновляет порядок (order) у силового сета.
     * @param set Объект StrengthSetModel с установленным новым порядком.
     */
    public void updateSetOrder(StrengthSetModel set) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.STRENGTH_SET_ORDER, set.getOrder());

        database.update(
                AppDataBase.STRENGTH_SET_DETAILS_TABLE,
                values,
                AppDataBase.STRENGTH_SET_ID + " = ?",
                new String[]{String.valueOf(set.getStrength_set_id())}
        );
    }


}