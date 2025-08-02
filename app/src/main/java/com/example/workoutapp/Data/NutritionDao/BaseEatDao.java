package com.example.workoutapp.Data.NutritionDao;

import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_AMOUNT;
import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_CALORIES;
import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_CARB;
import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_FAT;
import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_ID;
import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_MEASUREMENT_TYPE;
import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_NAME;
import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_PROTEIN;
import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.NutritionModels.FoodModel;

import java.util.ArrayList;
import java.util.List;

public class BaseEatDao {
    private final AppDataBase dbHelper;

    public BaseEatDao(AppDataBase dbHelper) {this.dbHelper = dbHelper;}

    // Метод добавления еды
    public void addEat(FoodModel eat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BASE_FOOD_NAME, eat.getFood_name());
        values.put(BASE_FOOD_PROTEIN, eat.getProtein());
        values.put(BASE_FOOD_FAT, eat.getFat());
        values.put(BASE_FOOD_CARB, eat.getCarb());
        values.put(BASE_FOOD_CALORIES, eat.getCalories());
        values.put(BASE_FOOD_AMOUNT, eat.getAmount());
        values.put(BASE_FOOD_MEASUREMENT_TYPE, eat.getMeasurement_type());

        db.insert(BASE_FOOD_TABLE, null, values);
        db.close();
    }

    // Метод удаления еды по ID
    public void deleteEat(int eatId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(BASE_FOOD_TABLE, BASE_FOOD_ID + " = ?", new String[]{String.valueOf(eatId)});
        db.close();
    }

    // Метод получения всех записей еды
    public List<FoodModel> getAllEat() {
        List<FoodModel> eatList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                BASE_FOOD_TABLE,
                null, // все столбцы
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(BASE_FOOD_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(BASE_FOOD_NAME));
                double protein = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_FOOD_PROTEIN));
                double fat = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_FOOD_FAT));
                double carb = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_FOOD_CARB));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_FOOD_CALORIES));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(BASE_FOOD_AMOUNT));
                String measurementType = cursor.getString(cursor.getColumnIndexOrThrow(BASE_FOOD_MEASUREMENT_TYPE));

                FoodModel eat = new FoodModel(id, name, protein, fat, carb, calories, amount, measurementType);
                eatList.add(eat);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return eatList;
    }

    public void logAllEat() {
        List<FoodModel> allEats = getAllEat();
        for (FoodModel eat : allEats) {
            Log.d("BaseEatDao", "ID: " + eat.getFood_id()
                    + ", Name: " + eat.getFood_name()
                    + ", Protein: " + eat.getProtein()
                    + ", Fat: " + eat.getFat()
                    + ", Carb: " + eat.getCarb()
                    + ", Calories: " + eat.getCalories()
                    + ", Amount: " + eat.getAmount()
                    + ", Measurement: " + eat.getMeasurement_type());
        }
    }
}
