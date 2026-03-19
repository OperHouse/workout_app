package com.example.workoutapp.Tools.SyncTools;

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

    public interface DownloadCallback {
        void onDownloaded(UserProfileModel profile);
        void onError(String error);
    }

    public interface SyncCallback {
        void onSuccess();

        void onFailure(String error);
    }


    /**
     * Метод только для скачивания данных из облака.
     */
    public void downloadProfile(DownloadCallback callback) {
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.get("name") != null) {
                        UserProfileModel cloudProfile = new UserProfileModel(
                                0,
                                doc.getString("name"),
                                doc.getDouble("height") != null ? doc.getDouble("height").floatValue() : 0,
                                doc.getLong("age") != null ? doc.getLong("age").intValue() : 0,
                                userId // передаем UID из Auth, так как в документе его может не быть
                        );

                        // Сохраняем локально
                        UserProfileDao dao = new UserProfileDao(MainActivity.getAppDataBase());
                        dao.insertOrUpdateProfile(cloudProfile);

                        if (callback != null) callback.onDownloaded(cloudProfile);
                    } else {
                        if (callback != null) callback.onError("No profile data in cloud");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
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
                    uploadProfile(localProfile, null);
                    Log.d("ProfileSync", "Локальные данные отправлены в пустое облако");
                }
            }
        });
    }

    /**
     * Отправка профиля на сервер.
     */

    /**
     * Отправка профиля на сервер с поддержкой Callback.
     */
    public void uploadProfile(UserProfileModel profile, SyncCallback callback) {
        if (userId == null || profile == null) {
            if (callback != null) callback.onFailure("User not logged in or profile null");
            return;
        }

        Map<String, Object> cloudData = new HashMap<>();
        cloudData.put("name", profile.getUserName());
        cloudData.put("height", profile.getUserHeight());
        cloudData.put("age", profile.getUserAge());
        cloudData.put("last_sync", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(cloudData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d("ProfileSync", "Firestore профиль обновлен");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileSync", "Ошибка синхронизации профиля", e);
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
}