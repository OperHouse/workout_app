package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_CALORIES;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_CARB;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_FAT;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_PROTEIN;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_TABLE;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_UID;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class FoodGainGoalDao {

    private final SQLiteDatabase db;

    public FoodGainGoalDao(SQLiteDatabase db) {
        this.db = db;
    }

    // =========================
    // Добавление новой цели по питанию
    // =========================
    public void insertGoal(FoodGainGoalModel goal) {
        if (goal == null) return;

        ContentValues values = new ContentValues();
        values.put(FOOD_GAIN_GOAL_CALORIES, goal.getFood_gain_goal_calories());
        values.put(FOOD_GAIN_GOAL_PROTEIN, goal.getFood_gain_goal_protein());
        values.put(FOOD_GAIN_GOAL_FAT, goal.getFood_gain_goal_fat());
        values.put(FOOD_GAIN_GOAL_CARB, goal.getFood_gain_goal_carb());
        values.put(FOOD_GAIN_GOAL_DATE, goal.getFood_gain_goal_date());
        values.put(FOOD_GAIN_GOAL_UID, goal.getFood_gain_goal_uid());

        db.insert(FOOD_GAIN_GOAL_TABLE, null, values);
    }

    // =========================
    // Получение последней цели по питанию
    // =========================
    public FoodGainGoalModel getLatestGoal() {
        FoodGainGoalModel goal = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    FOOD_GAIN_GOAL_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    FOOD_GAIN_GOAL_DATE + " DESC, " + FOOD_GAIN_GOAL_ID + " DESC",
                    "1"
            );

            if (cursor != null && cursor.moveToFirst()) {
                goal = new FoodGainGoalModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_CALORIES)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_PROTEIN)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_FAT)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_CARB)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_UID))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return goal;
    }

    // Новые методы для синхронизации
    public boolean isGoalUidExists(String uid) {
        if (uid == null) return false;
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + FOOD_GAIN_GOAL_TABLE + " WHERE food_gain_goal_uid = ?", new String[]{uid});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<FoodGainGoalModel> getAllGoals() {
        List<FoodGainGoalModel> list = new ArrayList<>();
        Cursor cursor = db.query(FOOD_GAIN_GOAL_TABLE, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new FoodGainGoalModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_CALORIES)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_PROTEIN)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_FAT)),
                        cursor.getFloat(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_CARB)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_UID))
                ));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void updateGoalUid(int id, String uid) {
        ContentValues cv = new ContentValues();
        cv.put("food_gain_goal_uid", uid);
        db.update(FOOD_GAIN_GOAL_TABLE, cv, FOOD_GAIN_GOAL_ID + " = ?", new String[]{String.valueOf(id)});
    }
}
