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
import android.database.sqlite.SQLiteDatabase;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;

public class FoodGainGoalDao {

    private final AppDataBase dbHelper;

    public FoodGainGoalDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление новой цели по питанию
    public void insertGoal(FoodGainGoalModel goal) {
        if (goal == null) return;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FOOD_GAIN_GOAL_CALORIES, goal.getCaloriesGoal());
        values.put(FOOD_GAIN_GOAL_PROTEIN, goal.getProteinGoal());
        values.put(FOOD_GAIN_GOAL_FAT, goal.getFatGoal());
        values.put(FOOD_GAIN_GOAL_CARB, goal.getCarbGoal());
        values.put(FOOD_GAIN_GOAL_DATE, goal.getDate());

        db.insert(FOOD_GAIN_GOAL_TABLE, null, values);
    }

    // Получение последней цели по питанию
    public FoodGainGoalModel getLatestGoal() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        FoodGainGoalModel goal = null;

        Cursor cursor = db.query(
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
            cursor.close();
        }
        return goal;
    }
}
