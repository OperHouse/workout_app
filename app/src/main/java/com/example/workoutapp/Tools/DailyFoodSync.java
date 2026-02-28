package com.example.workoutapp.Tools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.List;

public class DailyFoodSync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "DailyFoodSync";

    public void uploadEntry(DailyFoodTrackingModel model) {
        if (userId == null || model.getDaily_food_tracking_date() == null) return;

        // Используем ДАТУ как ID документа, чтобы избежать дублей
        db.collection("users").document(userId)
                .collection("daily_food").document(model.getDaily_food_tracking_date())
                .set(model, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Данные питания синхронизированы: " + model.getDaily_food_tracking_date()));
    }

    public void pullFromCloud(DailyFoodTrackingDao dao) {
        if (userId == null) return;

        db.collection("users").document(userId).collection("daily_food")
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots) {
                        DailyFoodTrackingModel cloudModel = doc.toObject(DailyFoodTrackingModel.class);
                        if (cloudModel != null && !dao.isUidExists(cloudModel.getDaily_food_tracking_uid())) {
                            dao.insertOrUpdate(cloudModel);
                        }
                    }
                });
    }

    public void pushLocalToCloud(DailyFoodTrackingDao dao) {
        if (userId == null) return;
        List<DailyFoodTrackingModel> entries = dao.getAllEntries();
        for (DailyFoodTrackingModel entry : entries) {
            if (entry.getDaily_food_tracking_uid() == null) {
                String newUid = UidGenerator.generateDailyFoodTrackingUid();
                entry.setDaily_food_tracking_uid(newUid);
                dao.updateUid(entry.getDaily_food_tracking_id(), newUid);
            }
            uploadEntry(entry);
        }
    }
}