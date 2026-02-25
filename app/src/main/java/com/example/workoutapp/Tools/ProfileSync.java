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

    // Синхронизация профиля: решаем, откуда брать данные
    public void syncProfile() {
        if (userId == null) return;

        UserProfileDao dao = new UserProfileDao(MainActivity.getAppDataBase());
        UserProfileModel localProfile = dao.getProfile();

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.contains("name") && doc.get("name") != null) {
                // 1. В ОБЛАКЕ ЕСТЬ ДАННЫЕ — скачиваем
                UserProfileModel cloudProfile = new UserProfileModel(
                        0,
                        doc.getString("name"),
                        doc.getDouble("height") != null ? doc.getDouble("height").floatValue() : 0,
                        doc.getLong("age") != null ? doc.getLong("age").intValue() : 0,
                        null
                );
                dao.insertOrUpdateProfile(cloudProfile);
                Log.d("ProfileSync", "Профиль скачан из облака");
            } else {
                // 2. В ОБЛАКЕ ПУСТО (или удалено)
                // ПРОВЕРКА: пушим только если локальное имя не null и не пустое
                if (localProfile != null && localProfile.getUserName() != null && !localProfile.getUserName().trim().isEmpty()) {
                    uploadProfile(localProfile);
                    Log.d("ProfileSync", "Локальные данные отправлены в облако");
                } else {
                    Log.d("ProfileSync", "И там, и там пусто. Ничего не делаем.");
                }
            }
        });
    }

    // Отправка данных на сервер (вызывается при редактировании профиля)
    public void uploadProfile(UserProfileModel profile) {
        if (userId == null || profile == null || profile.getUserName() == null) return;

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", profile.getUserName());
        userMap.put("height", profile.getUserHeight());
        userMap.put("age", profile.getUserAge());
        userMap.put("last_sync", System.currentTimeMillis());

        db.collection("users").document(userId).set(userMap, SetOptions.merge());
    }
}