package com.example.workoutapp.Data.ProfileDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_CALORIES;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_CARB;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_DATE;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_FAT;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_PROTEIN;
import static com.example.workoutapp.Data.Tables.AppDataBase.FOOD_GAIN_GOAL_TABLE;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;

import net.sqlcipher.database.SQLiteDatabase;

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
                        cursor.getString(cursor.getColumnIndexOrThrow(FOOD_GAIN_GOAL_DATE))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return goal;
    }
}
