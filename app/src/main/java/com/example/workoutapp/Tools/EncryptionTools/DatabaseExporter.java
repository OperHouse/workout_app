package com.example.workoutapp.Tools.EncryptionTools;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;

public class DatabaseExporter {

    /**
     * Экспортирует зашифрованную SQLCipher базу в обычную SQLite.
     *
     * @param context   - контекст приложения
     * @param exportName - имя файла для экспорта, например "WorkoutApp_plain.db"
     */
    public static void exportDatabase(Context context, String exportName) {
        try {
            // Получаем зашифрованную базу
            SQLiteDatabase encryptedDb = DatabaseProvider.get(context);

            // Путь для новой (нешифрованной) базы
            File exportFile = new File(context.getFilesDir(), exportName);
            if (exportFile.exists()) exportFile.delete();

            // Создаем пустую нешифрованную базу
            SQLiteDatabase decryptedDb = SQLiteDatabase.openOrCreateDatabase(
                    exportFile,
                    "",  // пустой пароль для обычной SQLite
                    null
            );

            // Экспортим данные через sqlcipher_export
            encryptedDb.rawExecSQL(
                    "ATTACH DATABASE '" + exportFile.getAbsolutePath() + "' AS plaintext KEY '';"
            );
            encryptedDb.rawExecSQL("SELECT sqlcipher_export('plaintext');");
            encryptedDb.rawExecSQL("DETACH DATABASE plaintext;");

            decryptedDb.close();

            Log.d("DatabaseExporter", "База успешно экспортирована: " + exportFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("DatabaseExporter", "Ошибка при экспорте базы", e);
        }
    }
    public static File exportDecryptedDatabase(Context context) {
        String exportName = "Workout_Backup_Plain.db";
        File exportFile = new File(context.getCacheDir(), "exports/" + exportName);

        try {
            // Создаем папку если её нет
            if (exportFile.getParentFile() != null && !exportFile.getParentFile().exists()) {
                exportFile.getParentFile().mkdirs();
            }
            if (exportFile.exists()) exportFile.delete();

            // Получаем доступ к текущей зашифрованной базе
            // Используем ваш DatabaseProvider
            SQLiteDatabase encryptedDb = DatabaseProvider.get(context);

            // Выполняем экспорт:
            // 1. Прикрепляем пустую базу без пароля
            // 2. Копируем туда структуру и данные
            // 3. Отключаем базу
            encryptedDb.rawExecSQL("ATTACH DATABASE '" + exportFile.getAbsolutePath() + "' AS plaintext KEY '';");
            encryptedDb.rawExecSQL("SELECT sqlcipher_export('plaintext');");
            encryptedDb.rawExecSQL("DETACH DATABASE plaintext;");

            Log.d("DatabaseExporter", "Расшифрованная база готова: " + exportFile.getAbsolutePath());
            return exportFile;

        } catch (Exception e) {
            Log.e("DatabaseExporter", "Ошибка экспорта: ", e);
            return null;
        }
    }
}
