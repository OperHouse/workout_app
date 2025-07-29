package com.example.workoutapp.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;
import com.example.workoutapp.NutritionModels.FoodModel;

import java.util.ArrayList;
import java.util.List;

public class MealFoodDao {
    private final AppDataBase dbHelper;
    public MealFoodDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }


    public List<FoodModel> getMealFoodsByIds(List<Long> ids) {
        List<FoodModel> foodList = new ArrayList<>();

        if (ids == null || ids.isEmpty()) {
            return foodList; // Возвращаем пустой список, если входной список пуст
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Формируем строку вида (?, ?, ?)
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) {
                placeholders.append(",");
            }
        }

        // Преобразуем список Long в массив строк
        String[] selectionArgs = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            selectionArgs[i] = String.valueOf(ids.get(i));
        }

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + AppDataBase.MEAL_FOOD_TABLE +
                        " WHERE " + AppDataBase.MEAL_FOOD_ID + " IN (" + placeholders + ")", selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                FoodModel eat = new FoodModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_PROTEIN)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_FAT)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_CARB)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_CALORIES)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_MEASUREMENT_TYPE))
                );
                foodList.add(eat);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return foodList;
    }

    public void deleteMealFoodById(int foodId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(AppDataBase.MEAL_FOOD_TABLE, AppDataBase.MEAL_FOOD_ID + " = ?", new String[]{String.valueOf(foodId)});
        db.close();
    }

    public long addSingleFood(FoodModel food) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_FOOD_NAME, food.getFood_name());
        values.put(AppDataBase.MEAL_FOOD_PROTEIN, food.getProtein());
        values.put(AppDataBase.MEAL_FOOD_FAT, food.getFat());
        values.put(AppDataBase.MEAL_FOOD_CARB, food.getCarb());
        values.put(AppDataBase.MEAL_FOOD_CALORIES, food.getCalories());
        values.put(AppDataBase.MEAL_FOOD_AMOUNT, food.getAmount());
        values.put(AppDataBase.MEAL_FOOD_MEASUREMENT_TYPE, food.getMeasurement_type());

        long id = db.insert(AppDataBase.MEAL_FOOD_TABLE, null, values);
        db.close();
        return id;
    }

    public void deleteMealFoodsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Формируем строку вида (?, ?, ?)
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) {
                placeholders.append(",");
            }
        }

        // Преобразуем список в массив строк для подстановки в запрос
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            args[i] = String.valueOf(ids.get(i));
        }

        db.delete(
                AppDataBase.MEAL_FOOD_TABLE,
                AppDataBase.MEAL_FOOD_ID + " IN (" + placeholders + ")",
                args
        );

        db.close();
    }



    //==============================Логирование======================================//

    public void logAllMealFoods() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                AppDataBase.MEAL_FOOD_TABLE,
                null, // выбираем все столбцы
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_NAME));
                double protein = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_PROTEIN));
                double fat = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_FAT));
                double carb = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_CARB));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_CALORIES));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_AMOUNT));
                String measurement = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_MEASUREMENT_TYPE));

                Log.d("MealFoodDao", "ID: " + id +
                        ", Name: " + name +
                        ", Protein: " + protein +
                        ", Fat: " + fat +
                        ", Carb: " + carb +
                        ", Calories: " + calories +
                        ", Amount: " + amount +
                        ", Measure: " + measurement);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
    }
}
