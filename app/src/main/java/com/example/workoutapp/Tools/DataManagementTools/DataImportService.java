package com.example.workoutapp.Tools.DataManagementTools;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DataImportService {

    private final SQLiteDatabase db;

    public DataImportService(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Импорт из JSON строки
     */
    public void importDataFromJson(String jsonString) throws Exception {
        if (jsonString == null || jsonString.isEmpty()) throw new Exception("Данные пусты");

        com.google.gson.reflect.TypeToken<Map<String, Object>> typeToken =
                new com.google.gson.reflect.TypeToken<Map<String, Object>>(){};
        Map<String, Object> root = new com.google.gson.Gson().fromJson(jsonString, typeToken.getType());
        Map<String, List<Map<String, Object>>> dataMap = (Map<String, List<Map<String, Object>>>) root.get("data");

        if (dataMap == null) throw new Exception("Блок 'data' не найден");

        db.execSQL("PRAGMA foreign_keys = OFF;");
        db.beginTransaction();
        try {
            for (String tableName : dataMap.keySet()) {
                List<Map<String, Object>> rows = dataMap.get(tableName);
                if (rows == null) continue;

                for (Map<String, Object> row : rows) {
                    ContentValues values = new ContentValues();
                    for (String key : row.keySet()) {
                        Object val = row.get(key);
                        if (val instanceof Double) {
                            Double d = (Double) val;
                            if (d == Math.floor(d)) values.put(key, d.longValue());
                            else values.put(key, d);
                        } else {
                            values.put(key, val != null ? val.toString() : null);
                        }
                    }
                    db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    /**
     * Импорт (восстановление) из .db файла
     * @param backupFile - файл базы данных, выбранный пользователем
     * @param password - пароль от базы (если используете SQLCipher)
     */
    public void importDataFromDbFile(File backupFile, String password) throws Exception {
        if (backupFile == null || !backupFile.exists()) throw new Exception("Файл не найден");

        // 1. Отключаем внешние ключи
        db.execSQL("PRAGMA foreign_keys = OFF;");

        try {
            // 2. Присоединяем файл бэкапа к текущей БД под именем 'backup_db'
            // Если пароля нет, используйте ""
            String passArg = (password == null) ? "" : password;
            db.execSQL("ATTACH DATABASE '" + backupFile.getAbsolutePath() + "' AS backup_db KEY '" + passArg + "';");

            // 3. Получаем список всех таблиц из присоединенной базы (кроме системных)
            Cursor cursor = db.rawQuery("SELECT name FROM backup_db.sqlite_master WHERE type='table' AND name NOT LIKE 'android_%' AND name NOT LIKE 'sqlite_%'", null);

            db.beginTransaction();
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String tableName = cursor.getString(0);
                        // 4. Копируем данные: INSERT OR REPLACE INTO текущая_таблица SELECT * FROM бэкап_таблица
                        db.execSQL("INSERT OR REPLACE INTO main." + tableName + " SELECT * FROM backup_db." + tableName);
                        Log.d("IMPORT", "Таблица " + tableName + " восстановлена");
                    } while (cursor.moveToNext());
                }
                db.setTransactionSuccessful();
            } finally {
                cursor.close();
                db.endTransaction();
            }

            // 5. Обязательно отсоединяем базу
            db.execSQL("DETACH DATABASE backup_db;");

        } catch (Exception e) {
            Log.e("IMPORT", "Ошибка восстановления БД", e);
            throw e;
        } finally {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }
}