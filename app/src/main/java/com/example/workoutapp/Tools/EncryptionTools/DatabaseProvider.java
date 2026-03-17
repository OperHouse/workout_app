package com.example.workoutapp.Tools.EncryptionTools;
import android.content.Context;
import com.example.workoutapp.Data.Tables.AppDataBase;
import net.sqlcipher.database.SQLiteDatabase;

public class DatabaseProvider {

    private static SQLiteDatabase database;

    public static synchronized SQLiteDatabase get(Context context) {
        if (database == null || !database.isOpen()) {
            String key = DatabaseKeyManager.getDatabaseKey(context);
            database = AppDataBase
                    .getInstance(context)
                    .getWritableDatabase(key);
        }
        return database;
    }
}