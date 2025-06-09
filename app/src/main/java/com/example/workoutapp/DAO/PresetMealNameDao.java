package com.example.workoutapp.DAO;

import static com.example.workoutapp.Data.AppDataBase.MEAL_PRESET_NAME;
import static com.example.workoutapp.Data.AppDataBase.MEAL_PRESET_NAME_ID;
import static com.example.workoutapp.Data.AppDataBase.MEAL_PRESET_NAME_TABLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.workoutapp.Data.AppDataBase;
import com.example.workoutapp.NutritionModels.MealPresetNameModel;

import java.util.ArrayList;
import java.util.List;

public class PresetMealNameDao {
    private final AppDataBase dbHelper;

    public PresetMealNameDao(AppDataBase dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Метод добавления названия пресета еды
    // В PresetMealNameDao
    public long addMealPresetName(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("meal_preset_name", name); // замените на ваше реальное имя поля
        long id = db.insert("meal_preset_name_table", null, values);
        db.close();
        return id;
    }

    // Метод удаления по ID
    public void deleteMealPresetName(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MEAL_PRESET_NAME_TABLE, MEAL_PRESET_NAME_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Получить все названия пресетов еды
    public List<MealPresetNameModel> getAllMealPresetNames() {
        List<MealPresetNameModel> nameList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                MEAL_PRESET_NAME_TABLE,
                null, // все столбцы
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MEAL_PRESET_NAME_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MEAL_PRESET_NAME));

                nameList.add(new MealPresetNameModel(id, name));
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return nameList;
    }


    // Вывести в лог всё содержимое таблицы
    public void logAllMealPresetNames() {
        List<MealPresetNameModel> all = getAllMealPresetNames();
        for (MealPresetNameModel model : all) {
            Log.d("PresetMealNameDao", "ID: " + model.getId() + ", Name: " + model.getName());
        }
    }
}
