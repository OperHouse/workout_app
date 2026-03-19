package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.List;

public class GeneralGoalSync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "GeneralGoalSync";

    // Интерфейс для результата отправки
    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Интерфейс для результата загрузки
    public interface DownloadCallback {
        void onDownloaded(List<GeneralGoalModel> goals);
        void onError(String error);
    }

    /**
     * ОТПРАВКА: Синхронизация одной общей цели с облаком.
     */
    public void uploadGoal(GeneralGoalModel goal, SyncCallback callback) {
        if (userId == null || goal == null || goal.getGeneral_goal_uid() == null) {
            if (callback != null) callback.onFailure("Данные отсутствуют или пользователь не авторизован");
            return;
        }

        db.collection("users").document(userId)
                .collection("general_goals").document(goal.getGeneral_goal_uid())
                .set(goal, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Общая цель синхронизирована: " + goal.getGeneral_goal_uid());
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка синхронизации: " + e.getMessage());
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    /**
     * ПОЛУЧЕНИЕ: Выгрузка всех общих целей из облака.
     */
    public void downloadGoals(DownloadCallback callback) {
        if (userId == null) {
            if (callback != null) callback.onError("Пользователь не авторизован");
            return;
        }

        db.collection("users").document(userId).collection("general_goals")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<GeneralGoalModel> cloudGoals = new ArrayList<>();
                    GeneralGoalDao goalDao = new GeneralGoalDao(MainActivity.getAppDataBase());

                    for (DocumentSnapshot doc : snapshots) {
                        GeneralGoalModel cloudGoal = doc.toObject(GeneralGoalModel.class);
                        if (cloudGoal != null && cloudGoal.getGeneral_goal_uid() != null) {
                            if (!goalDao.isGoalUidExists(cloudGoal.getGeneral_goal_uid())) {
                                goalDao.insertGoal(cloudGoal);
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