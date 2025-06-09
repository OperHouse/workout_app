package com.example.workoutapp.DAO;

import static com.example.workoutapp.Data.AppDataBase.BASE_EAT_AMOUNT;
import static com.example.workoutapp.Data.AppDataBase.BASE_EAT_CALORIES;
import static com.example.workoutapp.Data.AppDataBase.BASE_EAT_CARB;
import static com.example.workoutapp.Data.AppDataBase.BASE_EAT_FAT;
import static com.example.workoutapp.Data.AppDataBase.BASE_EAT_ID;
import static com.example.workoutapp.Data.AppDataBase.BASE_EAT_MEASUREMENT_TYPE;
import static com.example.workoutapp.Data.AppDataBase.BASE_EAT_NAME;
import static com.example.workoutapp.Data.AppDataBase.BASE_EAT_PROTEIN;
import static com.example.workoutapp.Data.AppDataBase.BASE_EAT_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;
import com.example.workoutapp.NutritionModels.EatModel;

import java.util.ArrayList;
import java.util.List;

public class BaseEatDao {
    private final AppDataBase dbHelper;

    public BaseEatDao(AppDataBase dbHelper) {this.dbHelper = dbHelper;}

    // Метод добавления еды
    public void addEat(EatModel eat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BASE_EAT_NAME, eat.getEat_name());
        values.put(BASE_EAT_PROTEIN, eat.getProtein());
        values.put(BASE_EAT_FAT, eat.getFat());
        values.put(BASE_EAT_CARB, eat.getCarb());
        values.put(BASE_EAT_CALORIES, eat.getCalories());
        values.put(BASE_EAT_AMOUNT, eat.getAmount());
        values.put(BASE_EAT_MEASUREMENT_TYPE, eat.getMeasurement_type());

        db.insert(BASE_EAT_TABLE, null, values);
        db.close();
    }

    // Метод удаления еды по ID
    public void deleteEat(int eatId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(BASE_EAT_TABLE, BASE_EAT_ID + " = ?", new String[]{String.valueOf(eatId)});
        db.close();
    }

    // Метод получения всех записей еды
    public List<EatModel> getAllEat() {
        List<EatModel> eatList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                BASE_EAT_TABLE,
                null, // все столбцы
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(BASE_EAT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(BASE_EAT_NAME));
                double protein = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_EAT_PROTEIN));
                double fat = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_EAT_FAT));
                double carb = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_EAT_CARB));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_EAT_CALORIES));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(BASE_EAT_AMOUNT));
                String measurementType = cursor.getString(cursor.getColumnIndexOrThrow(BASE_EAT_MEASUREMENT_TYPE));

                EatModel eat = new EatModel(id, name, protein, fat, carb, calories, amount, measurementType);
                eatList.add(eat);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return eatList;
    }

    public void logAllEat() {
        List<EatModel> allEats = getAllEat();
        for (EatModel eat : allEats) {
            Log.d("BaseEatDao", "ID: " + eat.getEat_id()
                    + ", Name: " + eat.getEat_name()
                    + ", Protein: " + eat.getProtein()
                    + ", Fat: " + eat.getFat()
                    + ", Carb: " + eat.getCarb()
                    + ", Calories: " + eat.getCalories()
                    + ", Amount: " + eat.getAmount()
                    + ", Measurement: " + eat.getMeasurement_type());
        }
    }
}
