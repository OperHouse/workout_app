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
import static com.example.workoutapp.Data.Tables.AppDataBase.BASE_FOOD_UID;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.workoutapp.Models.NutritionModels.FoodModel;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class BaseEatDao {

    private final SQLiteDatabase db;

    public BaseEatDao(SQLiteDatabase db) {
        this.db = db;
    }

    // Добавление еды
    public void addEat(FoodModel eat) {
        ContentValues values = getContentValues(eat);
        db.insert(BASE_FOOD_TABLE, null, values);
    }

    // Добавление еды с возвратом ID
    public long addFoodReturnID(FoodModel eat) {
        ContentValues values = getContentValues(eat);
        return db.insert(BASE_FOOD_TABLE, null, values);
    }

    // Удаление еды по ID
    public void deleteEat(int eatId) {
        db.delete(BASE_FOOD_TABLE, BASE_FOOD_ID + " = ?", new String[]{String.valueOf(eatId)});
    }

    // Получение всех продуктов
    public List<FoodModel> getAllEat() {
        List<FoodModel> eatList = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    BASE_FOOD_TABLE,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    eatList.add(getFoodModelFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return eatList;
    }

    // Получение продукта по ID
    public FoodModel getFoodById(long foodId) {
        FoodModel eat = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    BASE_FOOD_TABLE,
                    null,
                    BASE_FOOD_ID + " = ?",
                    new String[]{String.valueOf(foodId)},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                eat = getFoodModelFromCursor(cursor);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return eat;
    }

    // Получение ID последней вставленной строки
    public long getLastInsertedFoodId() {
        long id = -1;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return id;
    }

    // ========================= HELPERS ========================= //

    private ContentValues getContentValues(FoodModel eat) {
        ContentValues values = new ContentValues();
        values.put(BASE_FOOD_NAME, eat.getFood_name());
        values.put(BASE_FOOD_PROTEIN, eat.getProtein());
        values.put(BASE_FOOD_FAT, eat.getFat());
        values.put(BASE_FOOD_CARB, eat.getCarb());
        values.put(BASE_FOOD_CALORIES, eat.getCalories());
        values.put(BASE_FOOD_AMOUNT, eat.getAmount());
        values.put(BASE_FOOD_MEASUREMENT_TYPE, eat.getMeasurement_type());
        values.put(BASE_FOOD_UID, eat.getFood_uid());
        return values;
    }

    private FoodModel getFoodModelFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(BASE_FOOD_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(BASE_FOOD_NAME));
        double protein = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_FOOD_PROTEIN));
        double fat = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_FOOD_FAT));
        double carb = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_FOOD_CARB));
        double calories = cursor.getDouble(cursor.getColumnIndexOrThrow(BASE_FOOD_CALORIES));
        int amount = cursor.getInt(cursor.getColumnIndexOrThrow(BASE_FOOD_AMOUNT));
        String measurementType = cursor.getString(cursor.getColumnIndexOrThrow(BASE_FOOD_MEASUREMENT_TYPE));
        String uid = cursor.getString(cursor.getColumnIndexOrThrow(BASE_FOOD_UID));

        return new FoodModel(id, name, protein, fat, carb, calories, amount, measurementType, uid);
    }

    public void deleteAll() {
        db.delete(BASE_FOOD_TABLE, null, null);
    }

    public long getCount() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + BASE_FOOD_TABLE, null);
        long count = 0;
        if (cursor.moveToFirst()) count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    /**
     * Проверяет, существует ли продукт с таким UID в локальной базе.
     * Это предотвращает дублирование при загрузке из облака.
     */
    public boolean isFoodUidExists(String uid) {
        if (uid == null || uid.isEmpty()) return false;

        Cursor cursor = null;
        try {
            cursor = db.query(
                    BASE_FOOD_TABLE,
                    new String[]{BASE_FOOD_ID},
                    BASE_FOOD_UID + " = ?",
                    new String[]{uid},
                    null, null, null
            );
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // ======================================================
// INSERT OR UPDATE (UPSERT)
// ======================================================

    public void insertOrUpdate(FoodModel food) {

        if (food == null || food.getFood_uid() == null) return;

        db.beginTransaction();
        try {

            ContentValues values = getContentValues(food);

            // Пытаемся обновить
            int rows = db.update(
                    BASE_FOOD_TABLE,
                    values,
                    BASE_FOOD_UID + " = ?",
                    new String[]{food.getFood_uid()}
            );

            // Если ничего не обновилось — вставляем
            if (rows == 0) {
                db.insert(BASE_FOOD_TABLE, null, values);
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }


    // ======================================================
// DELETE BY UID
// ======================================================

    public void deleteByUid(String uid) {

        if (uid == null) return;

        db.delete(
                BASE_FOOD_TABLE,
                BASE_FOOD_UID + " = ?",
                new String[]{uid}
        );
    }

    /**
     * Получение продукта по его уникальному идентификатору (UID).
     * Используется для синхронизации через BaseFoodChangeHandler.
     */
    public FoodModel getFoodByUid(String uid) {
        if (uid == null || uid.isEmpty()) return null;

        FoodModel food = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    BASE_FOOD_TABLE,
                    null,
                    BASE_FOOD_UID + " = ?",
                    new String[]{uid},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                food = getFoodModelFromCursor(cursor);
            }
        } catch (Exception e) {
            android.util.Log.e("BaseEatDao", "Error getting food by UID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return food;
    }
}
