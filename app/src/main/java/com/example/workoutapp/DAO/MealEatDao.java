package com.example.workoutapp.DAO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;
import com.example.workoutapp.NutritionModels.EatModel;

import java.util.ArrayList;
import java.util.List;

public class MealEatDao {
    private final AppDataBase dbHelper;
    public MealEatDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void addFoodList(List<EatModel> eatList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (EatModel food : eatList) {
            ContentValues values = new ContentValues();
            values.put(AppDataBase.MEAL_EAT_NAME, food.getEat_name());
            values.put(AppDataBase.MEAL_EAT_PROTEIN, food.getProtein());
            values.put(AppDataBase.MEAL_EAT_FAT, food.getFat());
            values.put(AppDataBase.MEAL_EAT_CARB, food.getCarb());
            values.put(AppDataBase.MEAL_EAT_CALORIES, food.getCalories());
            values.put(AppDataBase.MEAL_EAT_AMOUNT, food.getAmount());
            values.put(AppDataBase.MEAL_EAT_MEASUREMENT_TYPE, food.getMeasurement_type());

            db.insert(AppDataBase.MEAL_EAT_TABLE, null, values);
        }

        db.close();
    }

    public List<Long> getAllFoodForMeal(long mealId) {
        List<Long> foodIds = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                AppDataBase.CONNECTING_MEAL_TABLE,
                new String[]{AppDataBase.CONNECTING_MEAL_EAT_ID},
                AppDataBase.CONNECTING_MEAL_NAME_ID + " = ?",
                new String[]{String.valueOf(mealId)},
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long foodId = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.CONNECTING_MEAL_EAT_ID));
                foodIds.add(foodId);
            }
            cursor.close();
        }

        db.close();
        return foodIds;
    }


    //==============================Логирование======================================//

    public void logAllMealEats() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                AppDataBase.MEAL_EAT_TABLE,
                null, // выбираем все столбцы
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_EAT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_EAT_NAME));
                double protein = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_EAT_PROTEIN));
                double fat = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_EAT_FAT));
                double carb = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_EAT_CARB));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_EAT_CALORIES));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_EAT_AMOUNT));
                String measurement = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_EAT_MEASUREMENT_TYPE));

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
