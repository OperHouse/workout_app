package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Tools.UidGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.List;

public class DailyActivitySync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "DailyActivitySync";

    public void uploadEntry(DailyActivityTrackingModel model) {
        if (userId == null || model.getDaily_activity_tracking_uid() == null) return;

        db.collection("users").document(userId)
                .collection("daily_activity").document(model.getDaily_activity_tracking_date())
                .set(model, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Данные за " + model.getDaily_activity_tracking_date() + " синхронизированы"));
    }

    public void pullFromCloud(DailyActivityTrackingDao dao) {
        if (userId == null) return;

        db.collection("users").document(userId).collection("daily_activity")
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots) {
                        DailyActivityTrackingModel cloudModel = doc.toObject(DailyActivityTrackingModel.class);
                        if (cloudModel != null && !dao.isUidExists(cloudModel.getDaily_activity_tracking_uid())) {
                            dao.insertOrUpdate(cloudModel);
                        }
                    }
                });
    }

    public void pushLocalToCloud(DailyActivityTrackingDao dao) {
        if (userId == null) return;
        List<DailyActivityTrackingModel> entries = dao.getAllEntries();
        for (DailyActivityTrackingModel entry : entries) {
            if (entry.getDaily_activity_tracking_uid() == null || entry.getDaily_activity_tracking_uid().isEmpty()) {
                String newUid = UidGenerator.generateDailyActivityUid();
                entry.setDaily_activity_tracking_uid(newUid);
                dao.updateUid(entry.getDaily_activity_tracking_id(), newUid);
            }
            uploadEntry(entry);
        }
    }
}