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

public class PresetMealNameDao {

    private final SQLiteDatabase db;

    public PresetMealNameDao(SQLiteDatabase db) {
        this.db = db;
    }

    // ========================= ADD ========================= //
    public long addMealPresetName(String name, String mealUid) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_PRESET_NAME, name);
        values.put(AppDataBase.MEAL_PRESET_UID, mealUid); // сохраняем uid сразу
        return db.insert(AppDataBase.MEAL_PRESET_NAME_TABLE, null, values);
    }

    public MealModel getMealById(long mealId, ConnectingMealPresetDao connectionDao, PresetEatDao eatDao) {

        // имя пресета
        String mealName = getMealPresetNameById(mealId);
        if (mealName == null) return null;

        // дата
        String mealDate = "";

        // связи
        List<Integer> eatIds = connectionDao.getEatIdsForPreset((int) mealId);

        // список еды
        List<FoodModel> eatList = new ArrayList<>();
        for (Integer eatId : eatIds) {
            FoodModel eat = eatDao.getPresetFoodById(eatId);
            if (eat != null) eatList.add(eat);
        }

        // uid
        String mealUid = getMealUidById(mealId);

        return new MealModel((int) mealId, mealName, mealUid, mealDate, eatList);
    }

    public MealModel getPresetMealByUid(String uid, ConnectingMealPresetDao connectionDao, PresetEatDao foodDao) {
        MealModel mealModel = null;
        Cursor cursor = null;
        try {
            // 1. Ищем пресет в таблице пресетов по UID
            cursor = db.query(
                    AppDataBase.MEAL_PRESET_NAME_TABLE,
                    null,
                    AppDataBase.MEAL_PRESET_UID + " = ?",
                    new String[]{uid},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // 2. Достаем локальный ID пресета
                int presetId = cursor.getInt(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_PRESET_NAME_ID));

                mealModel = getMealById(presetId, connectionDao, foodDao);
            }
        } catch (Exception e) {
            Log.e("PresetMealNameDao", "Error getPresetMealByUid: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return mealModel;
    }

    public String getMealDateById(long id) {

        String date = "";
        Cursor cursor = null;

        try {
            cursor = db.query(
                    AppDataBase.MEAL_PRESET_NAME_TABLE,
                    new String[]{AppDataBase.MEAL_DATA},
                    AppDataBase.MEAL_PRESET_NAME_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                date = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_DATA));
            }

        } finally {
            if (cursor != null) cursor.close();
        }

        return date;
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
            String date = meal.getMealData();


            String mealUid = getMealUidById(mealId);

            List<Integer> eatIds = connectionDao.getEatIdsForPreset(mealId);

            List<FoodModel> eatList = new ArrayList<>();
            for (Integer eatId : eatIds) {
                FoodModel eat = eatDao.getPresetFoodById(eatId);
                if (eat != null) {
                    eatList.add(eat);
                }
            }

            presetMeals.add(
                    new MealModel(mealId, name,  mealUid, date, eatList)
            );
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

    public long insertOrUpdate(MealModel meal) {

        if (meal == null || meal.getMeal_uid() == null)
            return -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AppDataBase.MEAL_PRESET_NAME, meal.getMeal_name());
            values.put(AppDataBase.MEAL_PRESET_UID, meal.getMeal_uid()); // <- обязательно правильное поле

            int rows = db.update(
                    AppDataBase.MEAL_PRESET_NAME_TABLE,
                    values,
                    AppDataBase.MEAL_PRESET_UID + " = ?",
                    new String[]{meal.getMeal_uid()} // <- правильно проверяем по meal_uid
            );

            long id;
            if (rows == 0) {
                id = db.insert(AppDataBase.MEAL_PRESET_NAME_TABLE, null, values);
            } else {
                id = getIdByUid(meal.getMeal_uid());
            }

            db.setTransactionSuccessful();
            return id;

        } finally {
            db.endTransaction();
        }
    }
    public void deleteByUid(String mealUid) {
        db.delete(
                AppDataBase.MEAL_PRESET_NAME_TABLE,
                "meal_preset_uid = ?",
                new String[]{mealUid}
        );
    }

    private long getIdByUid(String uid) {

        Cursor cursor = db.query(
                AppDataBase.MEAL_PRESET_NAME_TABLE,
                new String[]{AppDataBase.MEAL_PRESET_NAME_ID},
                AppDataBase.MEAL_PRESET_UID + " = ?",
                new String[]{uid},
                null, null, null
        );

        long id = -1;

        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }

        cursor.close();
        return id;
    }

    /**
     * Получает meal_uid по ID пресета
     */
    public String getMealUidById(long mealId) {
        String uid = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    AppDataBase.MEAL_PRESET_NAME_TABLE,
                    new String[]{AppDataBase.MEAL_PRESET_UID},
                    AppDataBase.MEAL_PRESET_NAME_ID + " = ?",
                    new String[]{String.valueOf(mealId)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                uid = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.MEAL_PRESET_UID));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return uid;
    }

    // ========================= SET UID ========================= //

    /**
     * Устанавливает meal_uid для пресета
     */
    public void setMealUid(long mealId, String mealUid) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.MEAL_PRESET_UID, mealUid);

        db.update(
                AppDataBase.MEAL_PRESET_NAME_TABLE,
                values,
                AppDataBase.MEAL_PRESET_NAME_ID + " = ?",
                new String[]{String.valueOf(mealId)}
        );
    }



}
