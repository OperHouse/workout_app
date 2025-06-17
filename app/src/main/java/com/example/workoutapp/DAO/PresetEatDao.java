package com.example.workoutapp.DAO;

import static com.example.workoutapp.Data.AppDataBase.PRESET_EAT_TABLE;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EAT_ID;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EAT_NAME;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EAT_PROTEIN;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EAT_FAT;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EAT_CARB;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EAT_CALORIES;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EAT_AMOUNT;
import static com.example.workoutapp.Data.AppDataBase.PRESET_EAT_MEASUREMENT_TYPE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;
import com.example.workoutapp.NutritionModels.EatModel;

import java.util.ArrayList;
import java.util.List;

public class PresetEatDao {
    private final AppDataBase dbHelper;

    public PresetEatDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Добавление записи preset eat и возврат её ID
    public void addPresetEat(EatModel eat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PRESET_EAT_NAME, eat.getEat_name());
        values.put(PRESET_EAT_PROTEIN, eat.getProtein());
        values.put(PRESET_EAT_FAT, eat.getFat());
        values.put(PRESET_EAT_CARB, eat.getCarb());
        values.put(PRESET_EAT_CALORIES, eat.getCalories());
        values.put(PRESET_EAT_AMOUNT, eat.getAmount());
        values.put(PRESET_EAT_MEASUREMENT_TYPE, eat.getMeasurement_type());


        db.insert(PRESET_EAT_TABLE, null, values);
        db.close();
    }

    // Удаление записи по ID
    public void deletePresetEat(int eatId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PRESET_EAT_TABLE, PRESET_EAT_ID + " = ?", new String[]{String.valueOf(eatId)});
        db.close();
    }

    // Получение всех записей preset eat
    public List<EatModel> getAllPresetEat() {
        List<EatModel> eatList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PRESET_EAT_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_EAT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EAT_NAME));
                double protein = cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_PROTEIN));
                double fat = cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_FAT));
                double carb = cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_CARB));
                double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_CALORIES));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_EAT_AMOUNT));
                String measurementType = cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EAT_MEASUREMENT_TYPE));

                EatModel eat = new EatModel(id, name, protein, fat, carb, calories, amount, measurementType);
                eatList.add(eat);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return eatList;
    }

    // Получение последнего ID из таблицы preset eat
    public int getLastInsertedPresetEatId() {
        int lastId = -1;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT MAX(" + PRESET_EAT_ID + ") FROM " + PRESET_EAT_TABLE,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            lastId = cursor.getInt(0);
            cursor.close();
        }

        db.close();
        return lastId;
    }

    public EatModel getPresetEatById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + PRESET_EAT_TABLE + " WHERE " + PRESET_EAT_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        EatModel eat = null;
        if (cursor.moveToFirst()) {
            eat = new EatModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_EAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EAT_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_PROTEIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_FAT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_CARB)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_CALORIES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_EAT_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EAT_MEASUREMENT_TYPE))
            );
        }

        cursor.close();
        db.close();
        return eat;
    }

    public EatModel findDuplicateEat(EatModel eat) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                PRESET_EAT_TABLE,
                null,
                PRESET_EAT_NAME + " = ? AND " +
                        PRESET_EAT_PROTEIN + " = ? AND " +
                        PRESET_EAT_FAT + " = ? AND " +
                        PRESET_EAT_CARB + " = ? AND " +
                        PRESET_EAT_CALORIES + " = ? AND " +
                        PRESET_EAT_AMOUNT + " = ? AND " +
                        PRESET_EAT_MEASUREMENT_TYPE + " = ?",
                new String[]{
                        eat.getEat_name(),
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

        EatModel duplicate = null;
        if (cursor != null && cursor.moveToFirst()) {
            duplicate = new EatModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_EAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EAT_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_PROTEIN)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_FAT)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_CARB)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(PRESET_EAT_CALORIES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(PRESET_EAT_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(PRESET_EAT_MEASUREMENT_TYPE))
            );
            cursor.close();
        }

        db.close();
        return duplicate;
    }


    // Логирование всех preset eat записей
    public void logAllPresetEat() {
        List<EatModel> allEats = getAllPresetEat();
        for (EatModel eat : allEats) {
            Log.d("PresetEatDao", "ID: " + eat.getEat_id()
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

