package com.example.workoutapp.Tools.DataManagementTools;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.google.gson.GsonBuilder;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataExportService {

    private final SQLiteDatabase db;

    public DataExportService(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Генерирует полную JSON строку со всеми данными приложения.
     */
    public String generateJsonExport() {
        Map<String, Object> fullExport = new HashMap<>();
        fullExport.put("export_date", System.currentTimeMillis());
        fullExport.put("app_version", 3); // Текущая версия БД

        Map<String, Object> dataMap = new HashMap<>();

        // Список всех таблиц из AppDataBase для экспорта
        String[] tables = {
                AppDataBase.BASE_EXERCISE_TABLE, AppDataBase.WORKOUT_PRESET_NAME_TABLE,
                AppDataBase.CONNECTING_WORKOUT_PRESET_TABLE, AppDataBase.WORKOUT_EXERCISE_TABLE,
                AppDataBase.STRENGTH_SET_DETAILS_TABLE, AppDataBase.CARDIO_SET_DETAILS_TABLE,
                AppDataBase.BASE_FOOD_TABLE, AppDataBase.PRESET_FOOD_TABLE,
                AppDataBase.MEAL_PRESET_NAME_TABLE, AppDataBase.CONNECTING_MEAL_PRESET_TABLE,
                AppDataBase.MEAL_NAME_TABLE, AppDataBase.MEAL_FOOD_TABLE,
                AppDataBase.CONNECTING_MEAL_TABLE, AppDataBase.USER_PROFILE_TABLE,
                AppDataBase.WEIGHT_HISTORY_TABLE, AppDataBase.DAILY_ACTIVITY_TRACKING_TABLE,
                AppDataBase.GENERAL_GOAL_TABLE, AppDataBase.ACTIVITY_GOAL_TABLE,
                AppDataBase.FOOD_GAIN_GOAL_TABLE, AppDataBase.DAILY_FOOD_TRACKING_TABLE
        };

        for (String tableName : tables) {
            dataMap.put(tableName, getAllRowsFromTable(tableName));
        }

        fullExport.put("data", dataMap);

        return new GsonBuilder().setPrettyPrinting().create().toJson(fullExport);
    }

    /**
     * Извлекает все строки из указанной таблицы в виде списка Map.
     */
    private List<Map<String, Object>> getAllRowsFromTable(String tableName) {
        List<Map<String, Object>> rows = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        int type = cursor.getType(i);
                        String colName = cursor.getColumnName(i);

                        switch (type) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                row.put(colName, cursor.getLong(i));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                row.put(colName, cursor.getDouble(i));
                                break;
                            case Cursor.FIELD_TYPE_NULL:
                                row.put(colName, null);
                                break;
                            default:
                                row.put(colName, cursor.getString(i));
                                break;
                        }
                    }
                    rows.add(row);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return rows;
    }


}