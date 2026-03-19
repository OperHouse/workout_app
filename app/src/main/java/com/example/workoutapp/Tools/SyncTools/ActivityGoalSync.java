package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.List;

public class ActivityGoalSync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "ActivityGoalSync";

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface DownloadCallback {
        void onDownloaded(List<ActivityGoalModel> goals);
        void onError(String error);
    }

    /**
     * ОТПРАВКА: Синхронизация одной цели с облаком.
     */
    public void uploadGoal(ActivityGoalModel goal, SyncCallback callback) {
        if (userId == null || goal == null || goal.getActivity_goal_uid() == null) {
            if (callback != null) callback.onFailure("Данные отсутствуют или пользователь не в сети");
            return;
        }

        db.collection("users").document(userId)
                .collection("activity_goals").document(goal.getActivity_goal_uid())
                .set(goal, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Цель синхронизирована: " + goal.getActivity_goal_uid());
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * ПОЛУЧЕНИЕ: Выгрузка всех целей из облака в локальную БД.
     */
    public void downloadGoals(DownloadCallback callback) {
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        db.collection("users").document(userId).collection("activity_goals")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<ActivityGoalModel> cloudGoals = new ArrayList<>();
                    ActivityGoalDao goalDao = new ActivityGoalDao(MainActivity.getAppDataBase());

                    for (DocumentSnapshot doc : snapshots) {
                        ActivityGoalModel cloudGoal = doc.toObject(ActivityGoalModel.class);
                        if (cloudGoal != null && cloudGoal.getActivity_goal_uid() != null) {
                            if (!goalDao.isGoalUidExists(cloudGoal.getActivity_goal_uid())) {
                                goalDao.addGoal(cloudGoal);
                            }
                            cloudGoals.add(cloudGoal);
                        }
                    }
                    if (callback != null) callback.onDownloaded(cloudGoals);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
}