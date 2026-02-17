package com.example.workoutapp.Data.NutritionDao;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Models.NutritionModels.MealNameModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PresetMealNameDao {

    private final SQLiteDatabase db;

    public PresetMealNameDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ========================= ADD ========================= //
    public long addMealPresetName(String name) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_PRESET_NAME, name);
        return db.insert(AppDataBase.MEAL_PRESET_NAME_TABLE, null, values);
    }

    // ========================= DELETE ========================= //
    public void deleteMealPresetName(long id) {
        db.delete(AppDataBase.MEAL_PRESET_NAME_TABLE, AppDataBase.MEAL_PRESET_NAME_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // ========================= UPDATE ========================= //
    public void updatePresetName(long presetId, String newName) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_PRESET_NAME, newName);
        db.update(AppDataBase.MEAL_PRESET_NAME_TABLE, values, AppDataBase.MEAL_PRESET_NAME_ID + " = ?", new String[]{String.valueOf(presetId)});
    }

    // ========================= GET ========================= //
    public String getMealPresetNameById(long id) {
        String presetName = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.MEAL_PRESET_NAME_TABLE,
                    new String[]{AppDataBase.MEAL_PRESET_NAME},
                    AppDataBase.MEAL_PRESET_NAME_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                presetName = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_PRESET_NAME));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return presetName;
    }

    public List<MealNameModel> getAllMealPresetNames() {
        List<MealNameModel> nameList = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    AppDataBase.MEAL_PRESET_NAME_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_PRESET_NAME_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_PRESET_NAME));
                    nameList.add(new MealNameModel(id, name));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return nameList;
    }

    /**
     * Получение всех preset meals с привязанными FoodModel
     */
    public List<MealModel> getAllPresetMealModels(
            ConnectingMealPresetDao connectionDao,
            PresetEatDao eatDao
    ) {
        List<MealModel> presetMeals = new ArrayList<>();
        List<MealNameModel> mealNames = getAllMealPresetNames();

        for (MealNameModel meal : mealNames) {
            int mealId = meal.getMeal_name_id();
            String name = meal.getMeal_name();

            // Получаем связи
            List<Integer> eatIds = connectionDao.getEatIdsForPreset(mealId);

            // Получаем FoodModel по id
            List<FoodModel> eatList = new ArrayList<>();
                for (Integer eatId : eatIds) {
                FoodModel eat = eatDao.getPresetFoodById(eatId);
                if (eat != null) {
                    eatList.add(eat);
                }
            }

            presetMeals.add(new MealModel(mealId, name, eatList));
        }

        return presetMeals;
    }

    public void deleteAll() {
        db.delete(AppDataBase.MEAL_PRESET_NAME_TABLE, null, null);
    }

    public long getCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + AppDataBase.MEAL_PRESET_NAME_TABLE, null);
        long count = 0;
        if (cursor.moveToFirst()) count = cursor.getLong(0);
        cursor.close();
        return count;
    }
}
