package com.example.workoutapp.Data.NutritionDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Tools.UidGenerator;

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
        if (food == null) return -1;

        if (food.getFood_uid() == null || food.getFood_uid().isEmpty()) {
            food.setFood_uid(UidGenerator.generateMealFoodUid() );
        }

        // 1. ПРОВЕРКА: Если UID пустой, генерируем его или выходим
        if (food.getFood_uid() == null || food.getFood_uid().isEmpty()) {
            Log.e("MealFoodDao", "Ошибка: food_uid is NULL для " + food.getFood_name());
            return -1;
        }

        // 2. БЕЗОПАСНЫЙ ПОИСК
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.MEAL_FOOD_TABLE,
                    new String[]{AppDataBase.MEAL_FOOD_ID},
                    AppDataBase.MEAL_FOOD_UID + " = ?",
                    new String[]{food.getFood_uid()}, // Теперь мы уверены, что тут не null
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_ID));
                cursor.close();
                return id;
            }
        } catch (Exception e) {
            Log.e("MealFoodDao", "Ошибка при поиске еды: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        // 3. ВСТАВКА (если не нашли)
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_FOOD_NAME, food.getFood_name());
        values.put(AppDataBase.MEAL_FOOD_PROTEIN, food.getProtein());
        values.put(AppDataBase.MEAL_FOOD_FAT, food.getFat());
        values.put(AppDataBase.MEAL_FOOD_CARB, food.getCarb());
        values.put(AppDataBase.MEAL_FOOD_CALORIES, food.getCalories());
        values.put(AppDataBase.MEAL_FOOD_AMOUNT, food.getAmount());
        values.put(AppDataBase.MEAL_FOOD_MEASUREMENT_TYPE, food.getMeasurement_type());
        values.put(AppDataBase.MEAL_FOOD_UID, food.getFood_uid());

        long id = db.insertWithOnConflict(AppDataBase.MEAL_FOOD_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
                            cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_MEASUREMENT_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_FOOD_UID))
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
        values.put(AppDataBase.MEAL_FOOD_UID, food.getFood_uid());

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
