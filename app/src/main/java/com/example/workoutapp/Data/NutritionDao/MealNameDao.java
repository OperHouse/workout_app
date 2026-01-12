package com.example.workoutapp.Data.NutritionDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.NutritionModels.MealNameModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MealNameDao {

    private final SQLiteDatabase db;

    public MealNameDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ========================= ADD ========================= //

    /**
     * Добавляет название приёма пищи и возвращает ID новой записи
     */
    public long insertMealName(String name, String date) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_NAME, name);
        values.put(AppDataBase.MEAL_DATA, date);

        long id = db.insert(AppDataBase.MEAL_NAME_TABLE, null, values);
        Log.d("MealNameDao", "Inserted meal name with id: " + id);
        return id;
    }

    // ========================= DELETE ========================= //

    public void deleteMealName(long id) {
        db.delete(AppDataBase.MEAL_NAME_TABLE, AppDataBase.MEAL_NAME_ID + " = ?", new String[]{String.valueOf(id)});
        Log.d("MealNameDao", "Deleted meal name with id: " + id);
    }

    // ========================= UPDATE ========================= //

    public void updateMealName(long mealId, String newName) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_NAME, newName);

        db.update(AppDataBase.MEAL_NAME_TABLE, values, AppDataBase.MEAL_NAME_ID + " = ?", new String[]{String.valueOf(mealId)});
        Log.d("MealNameDao", "Updated meal name with id: " + mealId);
    }

    // ========================= GET ========================= //

    public MealNameModel getMealNameModelById(long id) {
        MealNameModel mealModel = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.MEAL_NAME_TABLE,
                    null,
                    AppDataBase.MEAL_NAME_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                mealModel = new MealNameModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_NAME_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_DATA))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return mealModel;
    }

    public String getMealNameById(long id) {
        String name = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.MEAL_NAME_TABLE,
                    new String[]{AppDataBase.MEAL_NAME},
                    AppDataBase.MEAL_NAME_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_NAME));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return name;
    }

    public List<Integer> getMealNamesIdsByDate(String date) {
        List<Integer> idList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT " + AppDataBase.MEAL_NAME_ID +
                            " FROM " + AppDataBase.MEAL_NAME_TABLE +
                            " WHERE " + AppDataBase.MEAL_DATA + " = ?",
                    new String[]{date}
            );

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_NAME_ID));
                    idList.add(id);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return idList;
    }

    public List<MealNameModel> getAllMealNames() {
        List<MealNameModel> nameList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.MEAL_NAME_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    MealNameModel model = new MealNameModel(
                            cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_NAME_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_DATA))
                    );
                    nameList.add(model);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return nameList;
    }

    // ========================= CHECK EXISTENCE ========================= //

    public boolean checkIfMealExist(String name, String date) {
        boolean exists = false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT 1 FROM " + AppDataBase.MEAL_NAME_TABLE +
                            " WHERE " + AppDataBase.MEAL_NAME + " = ? AND " + AppDataBase.MEAL_DATA + " = ? LIMIT 1",
                    new String[]{name, date}
            );
            exists = cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
        }
        return exists;
    }

    // ========================= LAST INSERTED ID ========================= //

    public long getLastInsertedMealNameId() {
        long id = -1;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT MAX(" + AppDataBase.MEAL_NAME_ID + ") FROM " + AppDataBase.MEAL_NAME_TABLE,
                    null
            );
            if (cursor.moveToFirst()) {
                id = cursor.getLong(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return id;
    }
}
