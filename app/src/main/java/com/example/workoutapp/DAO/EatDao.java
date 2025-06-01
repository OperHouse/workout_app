package com.example.workoutapp.DAO;

import static com.example.workoutapp.Data.AppDataBase.EAT_AMOUNT;
import static com.example.workoutapp.Data.AppDataBase.EAT_CALORIES;
import static com.example.workoutapp.Data.AppDataBase.EAT_CARB;
import static com.example.workoutapp.Data.AppDataBase.EAT_FAT;
import static com.example.workoutapp.Data.AppDataBase.EAT_ID;
import static com.example.workoutapp.Data.AppDataBase.EAT_MEASUREMENT_TYPE;
import static com.example.workoutapp.Data.AppDataBase.EAT_NAME;
import static com.example.workoutapp.Data.AppDataBase.EAT_PROTEIN;
import static com.example.workoutapp.Data.AppDataBase.EAT_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;
import com.example.workoutapp.NutritionModels.EatModel;

import java.util.ArrayList;
import java.util.List;

public class EatDao {
    private final AppDataBase dbHelper;

    public EatDao(AppDataBase dbHelper) {this.dbHelper = dbHelper;}

    // Метод добавления еды
    public void addEat(EatModel eat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EAT_NAME, eat.getEat_name());
        values.put(EAT_PROTEIN, eat.getProtein());
        values.put(EAT_FAT, eat.getFat());
        values.put(EAT_CARB, eat.getCarb());
        values.put(EAT_CALORIES, eat.getCalories());
        values.put(EAT_AMOUNT, eat.getAmount());
        values.put(EAT_MEASUREMENT_TYPE, eat.getMeasurement_type());

        db.insert(EAT_TABLE, null, values);
        db.close();
    }

    // Метод удаления еды по ID
    public void deleteEat(int eatId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(EAT_TABLE, EAT_ID + " = ?", new String[]{String.valueOf(eatId)});
        db.close();
    }

    // Метод получения всех записей еды
    public List<EatModel> getAllEat() {
        List<EatModel> eatList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                EAT_TABLE,
                null, // все столбцы
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(EAT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(EAT_NAME));
                double protein = cursor.getDouble(cursor.getColumnIndexOrThrow(EAT_PROTEIN));
                double fat = cursor.getDouble(cursor.getColumnIndexOrThrow(EAT_FAT));
                double carb = cursor.getDouble(cursor.getColumnIndexOrThrow(EAT_CARB));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(EAT_CALORIES));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(EAT_AMOUNT));
                String measurementType = cursor.getString(cursor.getColumnIndexOrThrow(EAT_MEASUREMENT_TYPE));

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
            Log.d("EatDao", "ID: " + eat.getEat_id()
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
