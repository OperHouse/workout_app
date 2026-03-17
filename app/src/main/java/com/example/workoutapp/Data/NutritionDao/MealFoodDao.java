package com.example.workoutapp.Data.NutritionDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.NutritionModels.FoodModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MealFoodDao {

    private final SQLiteDatabase db;

    public MealFoodDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ========================= ADD FOOD ========================= //

    /**
     * Добавляет один элемент еды и возвращает его ID
     */
    public long addSingleFood(FoodModel food) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_FOOD_NAME, food.getFood_name());
        values.put(AppDataBase.MEAL_FOOD_PROTEIN, food.getProtein());
        values.put(AppDataBase.MEAL_FOOD_FAT, food.getFat());
        values.put(AppDataBase.MEAL_FOOD_CARB, food.getCarb());
        values.put(AppDataBase.MEAL_FOOD_CALORIES, food.getCalories());
        values.put(AppDataBase.MEAL_FOOD_AMOUNT, food.getAmount());
        values.put(AppDataBase.MEAL_FOOD_MEASUREMENT_TYPE, food.getMeasurement_type());

        long id = db.insert(AppDataBase.MEAL_FOOD_TABLE, null, values);
        Log.d("MealFoodDao", "Inserted food with id: " + id);
        return id;
    }

    // ========================= GET FOODS ========================= //

    /**
     * Получает список еды по списку ID
     */
    public List<FoodModel> getMealFoodsByIds(List<Long> ids) {
        List<FoodModel> foodList = new ArrayList<>();
        if (ids == null || ids.isEmpty()) return foodList;

        // Формируем placeholders
        StringBuilder placeholders = new StringBuilder();
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) placeholders.append(",");
            args[i] = String.valueOf(ids.get(i));
        }

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT * FROM " + AppDataBase.MEAL_FOOD_TABLE +
                            " WHERE " + AppDataBase.MEAL_FOOD_ID + " IN (" + placeholders + ")",
                    args
            );

            if (cursor.moveToFirst()) {
                do {
                    FoodModel food = new FoodModel(
                            cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_NAME)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_PROTEIN)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_FAT)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_CARB)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_CALORIES)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_AMOUNT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_MEASUREMENT_TYPE))
                    );
                    foodList.add(food);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return foodList;
    }

    // ========================= UPDATE FOOD ========================= //

    /**
     * Обновляет данные еды по ID
     */
    public void updateMealFood(FoodModel food) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_FOOD_NAME, food.getFood_name());
        values.put(AppDataBase.MEAL_FOOD_PROTEIN, food.getProtein());
        values.put(AppDataBase.MEAL_FOOD_FAT, food.getFat());
        values.put(AppDataBase.MEAL_FOOD_CARB, food.getCarb());
        values.put(AppDataBase.MEAL_FOOD_CALORIES, food.getCalories());
        values.put(AppDataBase.MEAL_FOOD_AMOUNT, food.getAmount());
        values.put(AppDataBase.MEAL_FOOD_MEASUREMENT_TYPE, food.getMeasurement_type());

        db.update(
                AppDataBase.MEAL_FOOD_TABLE,
                values,
                AppDataBase.MEAL_FOOD_ID + " = ?",
                new String[]{String.valueOf(food.getFood_id())}
        );
        Log.d("MealFoodDao", "Updated food with id: " + food.getFood_id());
    }

    // ========================= DELETE FOOD ========================= //

    /**
     * Удаляет один элемент еды
     */
    public void deleteMealFoodById(long foodId) {
        db.delete(AppDataBase.MEAL_FOOD_TABLE, AppDataBase.MEAL_FOOD_ID + " = ?", new String[]{String.valueOf(foodId)});
        Log.d("MealFoodDao", "Deleted food with id: " + foodId);
    }

    /**
     * Удаляет список еды по ID
     */
    public void deleteMealFoodsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;

        StringBuilder placeholders = new StringBuilder();
        String[] args = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            placeholders.append("?");
            if (i < ids.size() - 1) placeholders.append(",");
            args[i] = String.valueOf(ids.get(i));
        }

        db.delete(
                AppDataBase.MEAL_FOOD_TABLE,
                AppDataBase.MEAL_FOOD_ID + " IN (" + placeholders + ")",
                args
        );
        Log.d("MealFoodDao", "Deleted foods with ids: " + ids);
    }

    public void deleteAll() {
        db.delete(AppDataBase.MEAL_FOOD_TABLE, null, null);
    }

}
