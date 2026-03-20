package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.List;

public class DailyFoodSync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "DailyFoodSync";

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface DownloadCallback {
        void onDownloaded(List<DailyFoodTrackingModel> entries);
        void onError(String error);
    }

    /**
     * ОТПРАВКА: Синхронизация КБЖУ за конкретную дату.
     */
    public void uploadEntry(DailyFoodTrackingModel model, SyncCallback callback) {
        if (userId == null || model == null || model.getDaily_food_tracking_date() == null) {
            if (callback != null) callback.onFailure("Недостаточно данных для синхронизации");
            return;
        }

        // Используем дату как ID документа, чтобы данные за день перезаписывались/обновлялись
        db.collection("users").document(userId)
                .collection("daily_food_tracking")
                .document(model.getDaily_food_tracking_date())
                .set(model, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "КБЖУ за " + model.getDaily_food_tracking_date() + " синхронизированы");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка синхронизации КБЖУ: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * ВЫГРУЗКА: Получение всей истории КБЖУ из облака.
     */
    public void downloadEntries(DownloadCallback callback) {
        if (userId == null) {
            if (callback != null) callback.onError("Пользователь не авторизован");
            return;
        }

        db.collection("users").document(userId).collection("daily_food_tracking")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<DailyFoodTrackingModel> entries = new ArrayList<>();
                    DailyFoodTrackingDao dao = new DailyFoodTrackingDao(MainActivity.getAppDataBase());

                    for (DocumentSnapshot doc : snapshots) {
                        DailyFoodTrackingModel cloudModel = doc.toObject(DailyFoodTrackingModel.class);
                        if (cloudModel != null && cloudModel.getDaily_food_tracking_date() != null) {
                            // insertOrUpdate по дате
                            dao.insertOrUpdate(cloudModel);
                            entries.add(cloudModel);
                        }
                    }
                    if (callback != null) callback.onDownloaded(entries);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void uploadEntry(DailyFoodTrackingModel model) {
        uploadEntry(model, null);
    }
}