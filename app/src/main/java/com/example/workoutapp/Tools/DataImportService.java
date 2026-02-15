package com.example.workoutapp.Tools;

import android.content.ContentValues;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;
import java.util.Map;

public class DataImportService {

    private final SQLiteDatabase db;

    public DataImportService(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Основной метод импорта. Принимает строку JSON и записывает данные в таблицы.
     */
    public void importDataFromJson(String jsonString) throws Exception {
        if (jsonString == null || jsonString.isEmpty()) {
            throw new Exception("Файл пуст или поврежден");
        }

        // Парсим JSON структуру
        TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>(){};
        Map<String, Object> root = new Gson().fromJson(jsonString, typeToken.getType());

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> dataMap = (Map<String, List<Map<String, Object>>>) root.get("data");

        if (dataMap == null) {
            throw new Exception("Некорректный формат данных: отсутствует блок 'data'");
        }

        db.beginTransaction();
        try {
            for (String tableName : dataMap.keySet()) {
                List<Map<String, Object>> rows = dataMap.get(tableName);
                if (rows == null) continue;

                for (Map<String, Object> row : rows) {
                    ContentValues values = new ContentValues();
                    for (String key : row.keySet()) {
                        Object val = row.get(key);

                        // Обработка типов данных GSON (он часто читает числа как Double или Long)
                        if (val instanceof Double) {
                            values.put(key, (Double) val);
                        } else if (val instanceof Long) {
                            values.put(key, (Long) val);
                        } else {
                            values.put(key, val != null ? val.toString() : null);
                        }
                    }
                    // Используем REPLACE, чтобы не плодить дубликаты при совпадении ID
                    db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}