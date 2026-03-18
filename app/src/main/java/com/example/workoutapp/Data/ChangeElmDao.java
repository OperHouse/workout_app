package com.example.workoutapp.Data;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChangeElmDao {

    private final SQLiteDatabase db;
    private static final String TAG = "ChangeElmDao";

    public ChangeElmDao(SQLiteDatabase db) {
        this.db = db;
    }

    // =====================================================
    // ДОБАВЛЕНИЕ В ОЧЕРЕДЬ (Update/Change)
    // =====================================================
    public void enqueue(String uid, String type) {
        if (uid == null || uid.isEmpty()) return;

        ContentValues values = new ContentValues();
        values.put(AppDataBase.CHANGE_ELM_UID, uid);
        values.put(AppDataBase.CHANGE_ELM_TYPE, type);

        // Используем CONFLICT_REPLACE: если этот UID уже ждет обновления,
        // мы просто перезаписываем запись, не создавая дубликатов.
        db.insertWithOnConflict(AppDataBase.CHANGE_ELM_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "Добавлено в очередь изменений: " + uid + " [" + type + "]");
    }

    // =====================================================
    // ТО САМОЕ "ДВОЙНОЕ УДАЛЕНИЕ"
    // =====================================================
    /**
     * Вызывай этот метод, когда удаляешь сам прием пищи или пресет.
     * Он убирает UID из таблицы изменений, чтобы синхронизация не пыталась
     * обновить то, чего уже нет в локальной базе.
     */
    public void removeFromQueue(String uid) {
        if (uid == null) return;

        int rows = db.delete(
                AppDataBase.CHANGE_ELM_TABLE,
                AppDataBase.CHANGE_ELM_UID + " = ?",
                new String[]{uid}
        );

        if (rows > 0) {
            Log.d(TAG, "Объект " + uid + " удален из очереди изменений (т.к. удален совсем)");
        }
    }

    // =====================================================
    // ПОЛУЧЕНИЕ ВСЕХ ЗАДАЧ
    // =====================================================
    public List<ChangeTask> getAllTasks() {
        List<ChangeTask> tasks = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(AppDataBase.CHANGE_ELM_TABLE, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String uid = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.CHANGE_ELM_UID));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.CHANGE_ELM_TYPE));
                    tasks.add(new ChangeTask(uid, type));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return tasks;
    }

    // Вспомогательный класс для модели задачи
    public static class ChangeTask {
        public String uid;
        public String type;

        public ChangeTask(String uid, String type) {
            this.uid = uid;
            this.type = type;
        }
    }
}