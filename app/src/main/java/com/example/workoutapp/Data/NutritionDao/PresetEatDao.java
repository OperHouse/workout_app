package com.example.workoutapp.Data.NutritionDao;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.NutritionModels.FoodModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class PresetEatDao {

    private final SQLiteDatabase db;

    public PresetEatDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ========================= ADD ========================= //

    public long addPresetFood(FoodModel eat) {
        ContentValues values = getContentValues(eat);
        return db.insert(AppDataBase.PRESET_FOOD_TABLE, null, values);
    }

    // ========================= DELETE ========================= //

    public void deletePresetFood(long eatId) {
        db.delete(AppDataBase.PRESET_FOOD_TABLE,
                AppDataBase.PRESET_FOOD_ID + " = ?",
                new String[]{String.valueOf(eatId)});
    }

    public void deleteAll() {
        db.delete(AppDataBase.PRESET_FOOD_TABLE, null, null);
    }

    public void deleteConnectionsByMealUid(String mealUid) {
        long mealId = getMealIdByUid(mealUid);
        if (mealId == -1) return;

        db.delete(
                AppDataBase.CONNECTING_MEAL_PRESET_TABLE,
                AppDataBase.CONNECTING_MEAL_NAME_ID + " = ?",
                new String[]{String.valueOf(mealId)}
        );
    }

    private long getMealIdByUid(String mealUid) {
        Cursor cursor = db.query(
                AppDataBase.MEAL_PRESET_NAME_TABLE,
                new String[]{AppDataBase.MEAL_PRESET_NAME_ID},
                AppDataBase.PRESET_FOOD_UID + " = ?",
                new String[]{mealUid},
                null, null, null
        );

        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        return id;
    }

    // ========================= GET ========================= //

    public List<FoodModel> getAllPresetFood() {
        List<FoodModel> foodList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(AppDataBase.PRESET_FOOD_TABLE, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    foodList.add(mapCursorToFood(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return foodList;
    }

    public FoodModel getPresetFoodById(long id) {
        FoodModel food = null;
        Cursor cursor = null;
        try {
            cursor = db.query(AppDataBase.PRESET_FOOD_TABLE,
                    null,
                    AppDataBase.PRESET_FOOD_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                food = mapCursorToFood(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return food;
    }

    public FoodModel findDuplicateFood(FoodModel eat) {
        FoodModel duplicateFood = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.PRESET_FOOD_TABLE,
                    null,
                    AppDataBase.PRESET_FOOD_NAME + " = ? AND " +
                            AppDataBase.PRESET_FOOD_PROTEIN + " = ? AND " +
                            AppDataBase.PRESET_FOOD_FAT + " = ? AND " +
                            AppDataBase.PRESET_FOOD_CARB + " = ? AND " +
                            AppDataBase.PRESET_FOOD_CALORIES + " = ? AND " +
                            AppDataBase.PRESET_FOOD_AMOUNT + " = ? AND " +
                            AppDataBase.PRESET_FOOD_MEASUREMENT_TYPE + " = ?",
                    new String[]{
                            eat.getFood_name(),
                            String.valueOf(eat.getProtein()),
                            String.valueOf(eat.getFat()),
                            String.valueOf(eat.getCarb()),
                            String.valueOf(eat.getCalories()),
                            String.valueOf(eat.getAmount()),
                            eat.getMeasurement_type()
                    },
                    null, null, null
            );
            if (cursor.moveToFirst()) {
                duplicateFood = mapCursorToFood(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return duplicateFood;
    }

    public int getLastInsertedPresetFoodId() {
        int lastId = -1;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT MAX(" + AppDataBase.PRESET_FOOD_ID + ") FROM " + AppDataBase.PRESET_FOOD_TABLE, null);
            if (cursor.moveToFirst()) lastId = cursor.getInt(0);
        } finally {
            if (cursor != null) cursor.close();
        }
        return lastId;
    }

    public long getCount() {
        long count = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + AppDataBase.PRESET_FOOD_TABLE, null);
            if (cursor.moveToFirst()) count = cursor.getLong(0);
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    // ========================= INSERT OR UPDATE ========================= //

    public long insertOrUpdate(FoodModel food) {
        if (food == null || food.getFood_uid() == null) return -1;

        db.beginTransaction();
        try {
            ContentValues values = getContentValues(food);
            int rows = db.update(
                    AppDataBase.PRESET_FOOD_TABLE,
                    values,
                    AppDataBase.PRESET_FOOD_UID + " = ?",
                    new String[]{food.getFood_uid()}
            );

            long id;
            if (rows == 0) {
                id = db.insert(AppDataBase.PRESET_FOOD_TABLE, null, values);
            } else {
                id = getIdByUid(food.getFood_uid());
            }

            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    private long getIdByUid(String foodUid) {
        Cursor cursor = db.query(
                AppDataBase.PRESET_FOOD_TABLE,
                new String[]{AppDataBase.PRESET_FOOD_ID},
                AppDataBase.PRESET_FOOD_UID + " = ?",
                new String[]{foodUid},
                null, null, null
        );

        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        return id;
    }

    // ========================= HELPERS ========================= //

    private ContentValues getContentValues(FoodModel food) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.PRESET_FOOD_UID, food.getFood_uid());
        values.put(AppDataBase.PRESET_FOOD_NAME, food.getFood_name());
        values.put(AppDataBase.PRESET_FOOD_PROTEIN, roundToOneDecimal(food.getProtein()));
        values.put(AppDataBase.PRESET_FOOD_FAT, roundToOneDecimal(food.getFat()));
        values.put(AppDataBase.PRESET_FOOD_CARB, roundToOneDecimal(food.getCarb()));
        values.put(AppDataBase.PRESET_FOOD_CALORIES, roundToOneDecimal(food.getCalories()));
        values.put(AppDataBase.PRESET_FOOD_AMOUNT, roundToOneDecimal(food.getAmount()));
        values.put(AppDataBase.PRESET_FOOD_MEASUREMENT_TYPE, food.getMeasurement_type());
        return values;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private FoodModel mapCursorToFood(Cursor cursor) {
        return new FoodModel(
                cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.PRESET_FOOD_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.PRESET_FOOD_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.PRESET_FOOD_PROTEIN)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.PRESET_FOOD_FAT)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.PRESET_FOOD_CARB)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(AppDataBase.PRESET_FOOD_CALORIES)),
                cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.PRESET_FOOD_AMOUNT)),
                cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.PRESET_FOOD_MEASUREMENT_TYPE))
        );
    }
}