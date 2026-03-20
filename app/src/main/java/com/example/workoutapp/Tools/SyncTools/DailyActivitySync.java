package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.List;

public class DailyActivitySync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "DailyActivitySync";

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface DownloadCallback {
        void onDownloaded(List<DailyActivityTrackingModel> entries);
        void onError(String error);
    }

    /**
     * ОТПРАВКА: Синхронизация активности за день.
     * Используем дату как ID документа.
     */
    public void uploadEntry(DailyActivityTrackingModel model, SyncCallback callback) {
        if (userId == null || model == null || model.getDaily_activity_tracking_date() == null) {
            if (callback != null) callback.onFailure("Данные отсутствуют");
            return;
        }

        db.collection("users").document(userId)
                .collection("daily_activity")
                .document(model.getDaily_activity_tracking_date())
                .set(model, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Активность за " + model.getDaily_activity_tracking_date() + " синхронизирована");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка синхронизации активности: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * ВЫГРУЗКА: Получение всех данных об активности из облака.
     */
    public void downloadFromCloud(DownloadCallback callback) {
        if (userId == null) return;

        db.collection("users").document(userId).collection("daily_activity")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<DailyActivityTrackingModel> entries = new ArrayList<>();
                    DailyActivityTrackingDao dao = new DailyActivityTrackingDao(MainActivity.getAppDataBase());
                    for (DocumentSnapshot doc : snapshots) {
                        DailyActivityTrackingModel cloudModel = doc.toObject(DailyActivityTrackingModel.class);
                        if (cloudModel != null && cloudModel.getDaily_activity_tracking_uid() != null) {
                            if (!dao.isUidExists(cloudModel.getDaily_activity_tracking_uid())) {
                                dao.insertOrUpdate(cloudModel);
                            }
                            entries.add(cloudModel);
                        }
                    }
                    if (callback != null) callback.onDownloaded(entries);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    // Сохраняем для обратной совместимости
    public void uploadEntry(DailyActivityTrackingModel model) {
        uploadEntry(model, null);
    }
}