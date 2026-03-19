package com.example.workoutapp.Tools.SyncTools;

import com.example.workoutapp.Data.ProfileDao.WeightHistoryDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeightSync {
    private final FirebaseFirestore db;
    private final String userId;
    private static final String TAG = "WeightSync";

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface DownloadCallback {
        void onDownloaded(List<WeightHistoryModel> weightList);
        void onError(String error);
    }

    public WeightSync() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    /**
     * 1. ОТПРАВКА: Загрузка одной записи в облако
     */
    public void uploadWeightEntry(WeightHistoryModel weightEntry, SyncCallback callback) {
        if (userId == null || weightEntry == null || weightEntry.getWeight_history_uid() == null) {
            if (callback != null) callback.onFailure("Данные пусты или пользователь не авторизован");
            return;
        }

        db.collection("users").document(userId)
                .collection("weight_history")
                .document(weightEntry.getWeight_history_uid())
                .set(prepareWeightMap(weightEntry), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * 2. ПОЛУЧЕНИЕ: Скачивание всей истории веса с сервера
     */
    public void downloadWeightHistory(DownloadCallback callback) {
        if (userId == null) {
            if (callback != null) callback.onError("Пользователь не авторизован");
            return;
        }

        db.collection("users").document(userId)
                .collection("weight_history")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<WeightHistoryModel> cloudList = new ArrayList<>();
                    WeightHistoryDao dao = new WeightHistoryDao(MainActivity.getAppDataBase());

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        WeightHistoryModel entry = doc.toObject(WeightHistoryModel.class);
                        if (entry != null && entry.getWeight_history_uid() != null) {
                            // Сохраняем в локальную БД, если такой записи еще нет
                            if (!dao.isWeightUidExists(entry.getWeight_history_uid())) {
                                dao.addWeightEntry(entry);
                            }
                            cloudList.add(entry);
                        }
                    }
                    if (callback != null) callback.onDownloaded(cloudList);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    /**
     * Вспомогательный метод для маппинга полей
     */
    private Map<String, Object> prepareWeightMap(WeightHistoryModel weight) {
        Map<String, Object> map = new HashMap<>();
        map.put("weight_history_uid", weight.getWeight_history_uid());
        map.put("weight_history_value", weight.getWeight_history_value());
        map.put("weight_history_date", weight.getWeight_history_measurementDate());
        return map;
    }
}