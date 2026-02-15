package com.example.workoutapp.Tools.DataManagementTools;

import android.content.ContentValues;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;
import java.util.Map;

public class DataImportService {

    private final SQLiteDatabase db;

    public DataImportService(SQLiteDatabase db) {
        this.db = db;
    }

    public void importDataFromJson(String jsonString) throws Exception {
        if (jsonString == null || jsonString.isEmpty()) throw new Exception("Данные пусты");

        com.google.gson.reflect.TypeToken<Map<String, Object>> typeToken =
                new com.google.gson.reflect.TypeToken<Map<String, Object>>(){};
        Map<String, Object> root = new com.google.gson.Gson().fromJson(jsonString, typeToken.getType());
        Map<String, List<Map<String, Object>>> dataMap = (Map<String, List<Map<String, Object>>>) root.get("data");

        if (dataMap == null) throw new Exception("Блок 'data' не найден");

        // 1. ОТКЛЮЧАЕМ ключи до транзакции
        db.execSQL("PRAGMA foreign_keys = OFF;");

        db.beginTransaction();
        try {
            for (String tableName : dataMap.keySet()) {
                List<Map<String, Object>> rows = dataMap.get(tableName);
                if (rows == null) continue;

                for (Map<String, Object> row : rows) {
                    ContentValues values = new ContentValues();
                    for (String key : row.keySet()) {
                        if (key == null || key.equals("null")) continue;
                        Object val = row.get(key);

                        if (val instanceof Double) {
                            Double d = (Double) val;
                            if (d == Math.floor(d)) values.put(key, d.longValue());
                            else values.put(key, d);
                        } else if (val instanceof Long) {
                            values.put(key, (Long) val);
                        } else {
                            values.put(key, val != null ? val.toString() : null);
                        }
                    }

                    if (values.size() > 0) {
                        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            // 2. ВКЛЮЧАЕМ ключи обратно
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }
}