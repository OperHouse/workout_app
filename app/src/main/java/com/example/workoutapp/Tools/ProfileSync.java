package com.example.workoutapp.Tools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.UserProfileDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileSync {
    private final FirebaseFirestore db;
    private final String userId;

    public ProfileSync() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    /**
     * Полная синхронизация: решаем, нужно ли скачивать данные или отправлять локальные.
     */
    public void syncProfile() {
        if (userId == null) return;

        UserProfileDao dao = new UserProfileDao(MainActivity.getAppDataBase());
        UserProfileModel localProfile = dao.getProfile();

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.get("name") != null) {
                // 1. В ОБЛАКЕ ЕСТЬ ДАННЫЕ — скачиваем и обновляем локально
                UserProfileModel cloudProfile = new UserProfileModel(
                        0,
                        doc.getString("name"),
                        doc.getDouble("height") != null ? doc.getDouble("height").floatValue() : 0,
                        doc.getLong("age") != null ? doc.getLong("age").intValue() : 0,
                        null
                );
                dao.insertOrUpdateProfile(cloudProfile);
                Log.d("ProfileSync", "Профиль скачан из облака и сохранен локально");
            } else {
                // 2. В ОБЛАКЕ ПУСТО — отправляем локальные данные, если они есть
                if (localProfile != null && localProfile.getUserName() != null && !localProfile.getUserName().trim().isEmpty()) {
                    uploadProfile(localProfile);
                    Log.d("ProfileSync", "Локальные данные отправлены в пустое облако");
                }
            }
        });
    }

    /**
     * Отправка профиля на сервер.
     */
    public void uploadProfile(UserProfileModel profile) {
        if (userId == null || profile == null) return;

        Map<String, Object> cloudData = new HashMap<>();
        cloudData.put("name", profile.getUserName());
        cloudData.put("height", profile.getUserHeight());
        cloudData.put("age", profile.getUserAge());
        cloudData.put("last_sync", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(cloudData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("ProfileSync", "Firestore обновлен"))
                .addOnFailureListener(e -> Log.e("ProfileSync", "Ошибка синхронизации", e));
    }
}