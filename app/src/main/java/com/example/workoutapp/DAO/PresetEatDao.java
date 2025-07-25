package com.example.workoutapp.DAO;

import static com.example.workoutapp.Data.AppDataBase.PRESET_FOOD_TABLE;
import static com.example.workoutapp.Data.AppDataBase.PRESET_FOOD_ID;
import static com.example.workoutapp.Data.AppDataBase.PRESET_FOOD_NAME;
import static com.example.workoutapp.Data.AppDataBase.PRESET_FOOD_PROTEIN;
import static com.example.workoutapp.Data.AppDataBase.PRESET_FOOD_FAT;
import static com.example.workoutapp.Data.AppDataBase.PRESET_FOOD_CARB;
import static com.example.workoutapp.Data.AppDataBase.PRESET_FOOD_CALORIES;
import static com.example.workoutapp.Data.AppDataBase.PRESET_FOOD_AMOUNT;
import static com.example.workoutapp.Data.AppDataBase.PRESET_FOOD_MEASUREMENT_TYPE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;
import com.example.workoutapp.NutritionModels.FoodModel;

import java.util.ArrayList;
import java.util.List;

public class PresetEatDao {
    private final AppDataBase dbHelper;

    public PresetEatDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление записи preset eat и возврат её ID
    public void addPresetFood(FoodModel eat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PRESET_FOOD_NAME, eat.getFood_name());
        values.put(PRESET_FOOD_PROTEIN, roundToOneDecimal(eat.getProtein()));
        values.put(PRESET_FOOD_FAT, roundToOneDecimal(eat.getFat()));
        values.put(PRESET_FOOD_CARB, roundToOneDecimal(eat.getCarb()));
        values.put(PRESET_FOOD_CALORIES, roundToOneDecimal(eat.getCalories()));
        values.put(PRESET_FOOD_AMOUNT, roundToOneDecimal(eat.getAmount()));
        values.put(PRESET_FOOD_MEASUREMENT_TYPE, eat.getMeasurement_type());


        db.insert(PRESET_FOOD_TABLE, null, values);
        db.close();
    }

    // Удаление записи по ID
    public void deletePresetFood(int eatId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PRESET_FOOD_TABLE, PRESET_FOOD_ID + " = ?", new String[]{String.valueOf(eatId)});
        db.close();
    }

    // Получение всех записей preset eat
    public List<FoodModel> getAllPresetFood() {
        List<FoodModel> foodList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PRESET_FOOD_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_FOOD_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_FOOD_NAME));
                double protein = cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_PROTEIN));
                double fat = cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_FAT));
                double carb = cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_CARB));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_CALORIES));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_FOOD_AMOUNT));
                String measurementType = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_FOOD_MEASUREMENT_TYPE));

                FoodModel food = new FoodModel(id, name, protein, fat, carb, calories, amount, measurementType);
                foodList.add(food);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return foodList;
    }

    // Получение последнего ID из таблицы preset eat
    public int getLastInsertedPresetFoodId() {
        int lastId = -1;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT MAX(" + PRESET_FOOD_ID + ") FROM " + PRESET_FOOD_TABLE,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            lastId = cursor.getInt(0);
            cursor.close();
        }

        db.close();
        return lastId;
    }

    public FoodModel getPresetFoodById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + PRESET_FOOD_TABLE + " WHERE " + PRESET_FOOD_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        FoodModel food = null;
        if (cursor.moveToFirst()) {
            food = new FoodModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_FOOD_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PRESET_FOOD_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_PROTEIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_FAT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_CARB)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_CALORIES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_FOOD_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PRESET_FOOD_MEASUREMENT_TYPE))
            );
        }

        cursor.close();
        db.close();
        return food;
    }

    public FoodModel findDuplicateFood(FoodModel eat) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PRESET_FOOD_TABLE,
                null,
                PRESET_FOOD_NAME + " = ? AND " +
                        PRESET_FOOD_PROTEIN + " = ? AND " +
                        PRESET_FOOD_FAT + " = ? AND " +
                        PRESET_FOOD_CARB + " = ? AND " +
                        PRESET_FOOD_CALORIES + " = ? AND " +
                        PRESET_FOOD_AMOUNT + " = ? AND " +
                        PRESET_FOOD_MEASUREMENT_TYPE + " = ?",
                new String[]{
                        eat.getFood_name(),
                        String.valueOf(eat.getProtein()),
                        String.valueOf(eat.getFat()),
                        String.valueOf(eat.getCarb()),
                        String.valueOf(eat.getCalories()),
                        String.valueOf(eat.getAmount()),
                        eat.getMeasurement_type()
                },
                null,
                null,
                null
        );

        FoodModel duplicateFood = null;
        if (cursor != null && cursor.moveToFirst()) {
            duplicateFood = new FoodModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_FOOD_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PRESET_FOOD_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_PROTEIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_FAT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_CARB)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_FOOD_CALORIES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_FOOD_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PRESET_FOOD_MEASUREMENT_TYPE))
            );
            cursor.close();
        }

        db.close();
        return duplicateFood;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }


    // Логирование всех preset eat записей
    public void logAllPresetFood() {
        List<FoodModel> allFoods = getAllPresetFood();
        for (FoodModel food : allFoods) {
            Log.d("PresetEatDao", "ID: " + food.getFood_id()
                    + ", Name: " + food.getFood_name()
                    + ", Protein: " + food.getProtein()
                    + ", Fat: " + food.getFat()
                    + ", Carb: " + food.getCarb()
                    + ", Calories: " + food.getCalories()
                    + ", Amount: " + food.getAmount()
                    + ", Measurement: " + food.getMeasurement_type());
        }
    }
}

