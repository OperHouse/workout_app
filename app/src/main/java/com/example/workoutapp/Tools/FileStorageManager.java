package com.example.workoutapp.Tools;

import android.content.Context;

import java.io.File;

public class FileStorageManager {
    public static void saveFileToDownloads(Context context, File sourceFile, String fileName) {
        // Логика копирования файла из внутреннего кэша в папку Downloads
        File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS);
        File destinationFile = new File(downloadsDir, fileName);

        try (java.nio.channels.FileChannel source = new java.io.FileInputStream(sourceFile).getChannel();
             java.nio.channels.FileChannel destination = new java.io.FileOutputStream(destinationFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
            android.widget.Toast.makeText(context, "Файл сохранен в Загрузки", android.widget.Toast.LENGTH_SHORT).show();
        } catch (java.io.IOException e) {
            android.widget.Toast.makeText(context, "Ошибка сохранения", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
