package com.example.workoutapp.Data.NutritionDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
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
    public long insertMealName(String name, String date, String uid) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_NAME, name);
        values.put(AppDataBase.MEAL_DATA, date);
        values.put(AppDataBase.MEAL_NAME_UID, uid);

        long id = db.insert(AppDataBase.MEAL_NAME_TABLE, null, values);
        Log.d("MealNameDao", "Inserted meal name with id: " + id);
        return id;
    }

    public MealModel getMealByUid(String uid, ConnectingMealDao connectionDao, MealFoodDao foodDao) {
        MealModel mealModel = null;
        Cursor cursor = null;
        try {
            // 1. Ищем запись в таблице по UID
            cursor = db.query(
                    AppDataBase.MEAL_NAME_TABLE,
                    null,
                    AppDataBase.MEAL_NAME_UID + " = ?",
                    new String[]{uid},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // 2. Достаем локальный ID
                int mealId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_NAME_ID));

                // 3. Используем твой готовый метод для сборки всей модели с продуктами
                mealModel = getMealById(mealId, connectionDao, foodDao);
            }
        } catch (Exception e) {
            Log.e("MealNameDao", "Error getMealByUid: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return mealModel;
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
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_DATA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_NAME_UID))
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
                            cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_DATA)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_NAME_UID))
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

    public void deleteAll() {
        db.delete(AppDataBase.MEAL_NAME_TABLE, null, null);
    }

    public long getCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + AppDataBase.MEAL_NAME_TABLE, null);
        long count = 0;
        if (cursor.moveToFirst()) count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    // ========================= SET UID ========================= //

    public void setMealUid(long id, String uid) {

        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_NAME_UID, uid);

        int rows = db.update(
                AppDataBase.MEAL_NAME_TABLE,
                values,
                AppDataBase.MEAL_NAME_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        Log.d("MealNameDao",
                "setMealUid for id: " + id +
                        ", uid: " + uid +
                        ", rows updated: " + rows);
    }

    public MealModel getMealById(long mealId, ConnectingMealDao connectionDao, MealFoodDao foodDao) {
        // Получаем модель названия еды (MealNameModel)
        MealNameModel mealNameModel = getMealNameModelById(mealId);
        if (mealNameModel == null) return null;

        String mealName = mealNameModel.getMeal_name();
        String mealDate = mealNameModel.getMealData();
        String mealUid = mealNameModel.getMeal_uid();

        // Получаем список ID еды, связанных с этим приёмом пищи
        List<Long> foodIds = connectionDao.getFoodIdsForMeal((int) mealId);

        // Формируем список FoodModel через существующий метод
        List<FoodModel> foodList = new ArrayList<>();
        for (Long foodId : foodIds) {
            List<FoodModel> foods = foodDao.getMealFoodsByIds(
                    List.of(foodId) // передаём один ID в список
            );
            if (!foods.isEmpty()) {
                foodList.add(foods.get(0));
            }
        }

        // Возвращаем MealModel
        return new MealModel((int) mealId, mealName, mealUid, mealDate, foodList);
    }

}
