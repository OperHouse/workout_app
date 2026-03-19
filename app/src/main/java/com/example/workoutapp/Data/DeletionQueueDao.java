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

    public DeletionQueueDao(SQLiteDatabase db) {
        this.db = db;
    }

    // Добавить UID в очередь на удаление
    public void enqueue(String uid, String type) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.DELETION_UID, uid);
        values.put(AppDataBase.DELETION_TYPE, type);
        db.insert(AppDataBase.DELETION_QUEUE_TABLE, null, values);
        Log.d("DeletionQueue", "Enqueued for deletion: " + uid + " (" + type + ")");
    }

    public void enqueue(String uid, String type, String data) {
        ContentValues values = new ContentValues();
        values.put(AppDataBase.DELETION_UID, uid);
        values.put(AppDataBase.DELETION_TYPE, type);
        values.put(AppDataBase.DELETION_DATA, data);
        db.insert(AppDataBase.DELETION_QUEUE_TABLE, null, values);
        Log.d("DeletionQueue", "Enqueued for deletion: " + uid + " (" + type + ")" +  "(" + data + ")");
    }

    public List<DeletionTask> getAllPendingTasks() {
        List<DeletionTask> tasks = new ArrayList<>();
        Cursor cursor = db.query(AppDataBase.DELETION_QUEUE_TABLE, null, null, null, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String uid = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.DELETION_UID));
                        String type = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.DELETION_TYPE));
                        String data = cursor.getString(cursor.getColumnIndexOrThrow(AppDataBase.DELETION_DATA));
                        tasks.add(new DeletionTask(uid, type, data));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return tasks;
    }

    // Получить все записи из очереди
    public Cursor getAllPending() {
        return db.query(AppDataBase.DELETION_QUEUE_TABLE, null, null, null, null, null, null);
    }

    // Удалить из очереди после успешного удаления на сервере
    public void removeFromQueue(String uid) {
        int rowsDeleted = db.delete(AppDataBase.DELETION_QUEUE_TABLE,
                AppDataBase.DELETION_UID + " = ?", new String[]{uid});

        if (rowsDeleted > 0) {
            Log.d("DeletionQueue", "Успешно удалено из очереди: " + uid);
        } else {
            Log.e("DeletionQueue", "Ошибка: UID " + uid + " не найден в очереди (удаление не произошло)");
        }
    }
}
