package com.example.workoutapp.Data.NutritionDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.MEAL_DATA;
import static com.example.workoutapp.Data.Tables.AppDataBase.MEAL_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.MEAL_NAME_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.MEAL_NAME_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.NutritionModels.MealNameModel;

import java.util.ArrayList;
import java.util.List;

public class MealNameDao {
    private final AppDataBase dbHelper;

    public MealNameDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }



    // Метод добавления названия приема пищи
    //Возвращает id имени приема пищи для записи в связывающую таблицу (Связаны)
    public void insertMealName(String name, String date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MEAL_NAME, name);
        values.put(MEAL_DATA, date);
        db.insert(MEAL_NAME_TABLE, null, values);
        db.close();
    }

    public void deleteMealName(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MEAL_NAME_TABLE, MEAL_NAME_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateMealName(int presetId, String newName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MEAL_NAME, newName);

        db.update(MEAL_NAME_TABLE, values, MEAL_NAME_ID + " = ?", new String[]{String.valueOf(presetId)});
        db.close();
    }

    public String getMealNameById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String mealName = null;

        Cursor cursor = db.query(
                MEAL_NAME_TABLE,
                new String[]{MEAL_NAME},
                MEAL_NAME_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mealName = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_NAME));
            }
            cursor.close();
        }
        db.close();
        return mealName;
    }
    // Метод для получения массива ID имен по дате
    public List<Integer> getMealNamesIdsByDate(String date) {
        List<Integer> idList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Запрос для поиска всех записей по дате
        String query = "SELECT " + MEAL_NAME_ID + " FROM " + MEAL_NAME_TABLE + " WHERE " + MEAL_DATA + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date});

        // Если курсор не пустой, извлекаем все ID
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_NAME_ID));
                idList.add(id);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return idList;
    }

    // Получить все названия пресетов еды
    public List<MealNameModel> getAllMealNames() {
        List<MealNameModel> nameList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MEAL_NAME_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_NAME_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_NAME));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_DATA));

                nameList.add(new MealNameModel(id, name, data));
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return nameList;
    }

    public boolean checkIfMealExist(String name, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + MEAL_NAME_TABLE +
                " WHERE " + MEAL_NAME + " = ? AND " + MEAL_DATA + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name, date});

        boolean exists = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                exists = count > 0;
            }
            cursor.close();
        }

        db.close();
        return exists;
    }
    public long getLastInsertedMealNameId() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long id = -1;

        Cursor cursor = db.rawQuery(
                "SELECT MAX(" + MEAL_NAME_ID + ") FROM " + MEAL_NAME_TABLE,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                id = cursor.getLong(0);
            }
            cursor.close();
        }

        db.close();
        return id;
    }

    public MealNameModel getMealNameModelById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MealNameModel mealModel = null;

        Cursor cursor = db.query(
                MEAL_NAME_TABLE,
                null,
                MEAL_NAME_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int mealId = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_NAME_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_NAME));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_DATA)); // mealData

            mealModel = new MealNameModel(mealId, name, data);
            cursor.close();
        }

        db.close();
        return mealModel;
    }

    //==============================Логирование======================================//

    public void logAllMealNames() {
        List<MealNameModel> all = getAllMealNames();
        for (MealNameModel model : all) {
            Log.d("MealNameDao", "Data: " + model.getMealData() + ", ID: " + model.getMeal_name_id() + ", Name: " + model.getMeal_name());
        }
    }
}
