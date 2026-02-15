package com.example.workoutapp.Tools.DataManagementTools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileStorageManager {

    /**
     * Обработка экспорта JSON: создание файла и выбор действия (сохранить или отправить)
     */
    public static void processExport(Activity activity, String jsonString) {
        try {
            // 1. Подготовка временного файла
            File cachePath = new File(activity.getCacheDir(), "exports");
            if (!cachePath.exists()) cachePath.mkdirs();
            File tempFile = new File(cachePath, "workout_data_backup.json");

            try (FileOutputStream stream = new FileOutputStream(tempFile)) {
                stream.write(jsonString.getBytes());
            }

            // 2. Создание диалога с нужным расположением
            activity.runOnUiThread(() -> {
                AlertDialog dialog = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                        .setTitle("Экспорт данных")
                        .setMessage("Выберите действие для файла бэкапа:")
                        // Положительная кнопка (всегда справа по стандарту Android)
                        .setPositiveButton("Отмена", (d, w) -> d.dismiss())
                        // Нейтральная кнопка (всегда слева/посередине)
                        .setNeutralButton("Сохранить в Загрузки", (d, w) -> {
                            String fileName = "workout_backup_" + System.currentTimeMillis() + ".json";
                            saveFileToDownloads(activity, tempFile, fileName);
                        })
                        // Отрицательная кнопка (будет рядом с нейтральной)
                        .setNegativeButton("Поделиться", (d, w) -> {
                            shareFile(activity, tempFile, "application/json");
                        })
                        .create();

                dialog.show();

                // Хаки для центровки текста в кнопках (опционально)
                Button btnSave = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                Button btnShare = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                if (btnSave != null) btnSave.setAllCaps(false);
                if (btnShare != null) btnShare.setAllCaps(false);

            });

        } catch (IOException e) {
            Log.e("FileStorageManager", "Export error", e);
            activity.runOnUiThread(() -> Toast.makeText(activity, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Универсальный метод для отправки любого файла через другие приложения
     */
    public static void shareFile(Activity activity, File file, String mimeType) {
        try {
            Uri contentUri = FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".fileprovider", file);

            if (contentUri != null) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                activity.startActivity(Intent.createChooser(intent, "Поделиться через..."));
            }
        } catch (Exception e) {
            Log.e("FileStorageManager", "Sharing error", e);
            Toast.makeText(activity, "Ошибка при отправке файла", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Физическое копирование файла в папку Downloads телефона
     */
    public static void saveFileToDownloads(Context context, File sourceFile, String fileName) {
        File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS);
        File destinationFile = new File(downloadsDir, fileName);

        try (FileChannel source = new FileInputStream(sourceFile).getChannel();
             FileChannel destination = new FileOutputStream(destinationFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
            Toast.makeText(context, "Файл сохранен в Загрузки", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("FileStorageManager", "Save error", e);
            Toast.makeText(context, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }
}