package com.example.workoutapp.Tools;

import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudSyncHelper {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String uid;

    public CloudSyncHelper() {
        this.uid = FirebaseAuth.getInstance().getUid();
    }

    // 1. СИНХРОНИЗАЦИЯ АКТИВНОСТИ (Daily Activity)
    public void syncDailyActivity(List<DailyActivityTrackingModel> localData) {
        if (uid == null || localData == null) return;

        for (DailyActivityTrackingModel model : localData) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", model.getDaily_activity_tracking_date());
            map.put("steps", model.getDaily_activity_tracking_steps());
            map.put("calories", model.getDaily_activity_tracking_caloriesBurned());

            db.collection("users").document(uid)
                    .collection("daily_activity")
                    .document(model.getDaily_activity_tracking_date()) // Используем дату как ID, чтобы не плодить дубликаты
                    .set(map, SetOptions.merge());
        }
    }

    // 2. СИНХРОНИЗАЦИЯ ПРОФИЛЯ
    public void syncUserProfile(String name, double height, int age) {
        if (uid == null) return;

        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("height", height);
        profile.put("age", age);

        db.collection("users").document(uid)
                .collection("info").document("profile")
                .set(profile, SetOptions.merge());
    }

    // 3. СИНХРОНИЗАЦИЯ ТРЕНИРОВОК (Пример логики)
    // Так как в SQLite у тебя связи (Foreign Key), в Firebase мы вкладываем детали в один документ
    public void syncWorkout(String workoutId, String name, String date, List<Map<String, Object>> sets) {
        if (uid == null) return;

        Map<String, Object> workout = new HashMap<>();
        workout.put("name", name);
        workout.put("date", date);
        workout.put("sets", sets); // Массив подходов прямо внутри документа тренировки

        db.collection("users").document(uid)
                .collection("workouts").document(workoutId)
                .set(workout, SetOptions.merge());
    }
}