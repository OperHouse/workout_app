package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.List;

public class ActivityGoalSync {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getUid();
    private final String TAG = "ActivityGoalSync";

    // Отправка одной цели в облако
    public void uploadGoal(ActivityGoalModel goal) {
        if (userId == null || goal.getActivity_goal_uid() == null) return;

        db.collection("users").document(userId)
                .collection("activity_goals").document(goal.getActivity_goal_uid())
                .set(goal, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Цель синхронизирована"))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка: " + e.getMessage()));
    }

    // Загрузка целей из облака (Pull)
    public void pullGoalsFromCloud(ActivityGoalDao goalDao) {
        if (userId == null) return;

        db.collection("users").document(userId).collection("activity_goals")
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots) {
                        ActivityGoalModel cloudGoal = doc.toObject(ActivityGoalModel.class);
                        if (cloudGoal != null && !goalDao.isGoalUidExists(cloudGoal.getActivity_goal_uid())) {
                            goalDao.addGoal(cloudGoal);
                        }
                    }
                });
    }

    public void pushLocalGoalsToCloud(ActivityGoalDao goalDao) {
        if (userId == null || goalDao == null) return;

        // Получаем все цели из локальной базы
        List<ActivityGoalModel> localGoals = goalDao.getAllGoals();

        for (ActivityGoalModel goal : localGoals) {
            // Если UID нет (старая запись), генерируем его и обновляем локально
            if (goal.getActivity_goal_uid() == null || goal.getActivity_goal_uid().isEmpty()) {
                String newUid = "AG_" + java.util.UUID.randomUUID().toString();
                goal.setActivity_goal_uid(newUid);
                goalDao.updateGoalUid(goal.getActivity_goal_id(), newUid); // Метод нужно добавить в DAO
            }

            // Отправляем в облако
            uploadGoal(goal);
        }
    }
}