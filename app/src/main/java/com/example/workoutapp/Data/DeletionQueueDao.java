package com.example.workoutapp.Data;

import android.content.ContentValues;
import android.util.Log;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Models.DeletionTask;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DeletionQueueDao {
    private final SQLiteDatabase db;
    private static final String TAG = "DeletionQueueDao";

    public DeletionQueueDao(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Добавить задачу в очередь на удаление.
     * Используется для простых удалений (только UID).
     */
    public void enqueue(String uid, String type) {
        enqueue(uid, type, null);
    }

    /**
     * Добавить задачу в очередь на удаление с дополнительными данными.
     * CONFLICT_REPLACE гарантирует, что если UID уже есть в очереди, данные обновятся.
     */
    public void enqueue(String uid, String type, String data) {
        if (uid == null) return;

        ContentValues values = new ContentValues();
        values.put(AppDataBase.DELETION_UID, uid);
        values.put(AppDataBase.DELETION_TYPE, type);
        values.put(AppDataBase.DELETION_DATA, data);

        try {
            db.insertWithOnConflict(AppDataBase.DELETION_QUEUE_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d(TAG, "Добавлено в очередь на удаление: " + uid + " (Тип: " + type + ")");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при добавлении в очередь удаления: " + e.getMessage());
        }
    }

    /**
     * Получить все активные задачи из очереди в виде списка моделей.
     */
    public List<DeletionTask> getAllPendingTasks() {
        List<DeletionTask> tasks = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(AppDataBase.DELETION_QUEUE_TABLE, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int uidIndex = cursor.getColumnIndexOrThrow(AppDataBase.DELETION_UID);
                int typeIndex = cursor.getColumnIndexOrThrow(AppDataBase.DELETION_TYPE);
                int dataIndex = cursor.getColumnIndexOrThrow(AppDataBase.DELETION_DATA);

                do {
                    String uid = cursor.getString(uidIndex);
                    String type = cursor.getString(typeIndex);
                    String data = cursor.getString(dataIndex);

                    tasks.add(new DeletionTask(uid, type, data));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при чтении очереди удаления: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return tasks;
    }

    /**
     * Удалить задачу из очереди после успешной синхронизации с Firebase.
     */
    public void removeFromQueue(String uid) {
        if (uid == null) return;

        try {
            int rowsDeleted = db.delete(AppDataBase.DELETION_QUEUE_TABLE,
                    AppDataBase.DELETION_UID + " = ?", new String[]{uid});

            if (rowsDeleted > 0) {
                Log.d(TAG, "Удалено из локальной очереди: " + uid);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении из очереди: " + e.getMessage());
        }
    }

    /**
     * Проверка, пуста ли очередь.
     */
    public boolean isEmpty() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + AppDataBase.DELETION_QUEUE_TABLE, null);
        boolean empty = true;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                empty = cursor.getInt(0) == 0;
            }
            cursor.close();
        }
        return empty;
    }
}