package com.example.workoutapp.Data.EncryptionTools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

public class DatabaseKeyManager {
    private static final String PREF_KEY = "db_key";

    public static String getDatabaseKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("db_prefs", Context.MODE_PRIVATE);
        String key = prefs.getString(PREF_KEY, null);

        if (key == null) {
            // Генерируем случайный 256-бит ключ
            byte[] newKey = new byte[32];
            new java.security.SecureRandom().nextBytes(newKey);
            key = Base64.encodeToString(newKey, Base64.NO_WRAP);
            prefs.edit().putString(PREF_KEY, key).apply();
        }

        return key;
    }
}

