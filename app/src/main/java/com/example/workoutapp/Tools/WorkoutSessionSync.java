package com.example.workoutapp.Tools;

import android.util.Log;

import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.Models.WorkoutModels.WorkoutSessionModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutSessionSync {

    private final FirebaseFirestore db;
    private final String userId;
    private static final String TAG = "WorkoutSessionSync";

    public WorkoutSessionSync() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    /**
     * Точка входа для полной синхронизации.
     */
    public void startWorkoutSync(List<ExerciseModel> localExercises, Map<String, com.google.firebase.firestore.DocumentSnapshot> cloudMap) {
        if (userId == null) return;

        // 1. Сначала дополняем облако локальными данными
        syncAllWorkouts(localExercises);

        // 2. Затем дополняем телефон данными из облака
        for (String cloudDate : cloudMap.keySet()) {
            WorkoutSessionModel cloudSession = cloudMap.get(cloudDate).toObject(WorkoutSessionModel.class);
            if (cloudSession != null) {
                saveCloudSessionToSQLite(cloudSession);
            }
        }
    }

    /**
     * Группирует локальные упражнения и отправляет их в облако методом слияния.
     */
    public void syncAllWorkouts(List<ExerciseModel> allExercises) {
        if (userId == null || allExercises == null) return;

        Map<String, List<ExerciseModel>> grouped = groupExercisesByDate(allExercises);
        for (String date : grouped.keySet()) {
            uploadWorkoutSession(new WorkoutSessionModel(date, grouped.get(date)));
        }
    }

    /**
     * Загрузка тренировки: Скачивает текущие данные сервера и добавляет новые упражнения.
     */
    public void uploadWorkoutSession(WorkoutSessionModel session) {
        if (userId == null || session == null) return;

        DocumentReference docRef = db.collection("users").document(userId)
                .collection("workouts").document(session.getWorkoutDate());

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            List<Map<String, Object>> finalExercises = new ArrayList<>();

            // Если на сервере уже есть упражнения - берем их за основу
            if (documentSnapshot.exists()) {
                List<Map<String, Object>> existing = (List<Map<String, Object>>) documentSnapshot.get("exercises");
                if (existing != null) finalExercises.addAll(existing);
            }

            // Добавляем локальные, если их еще нет на сервере по имени
            for (ExerciseModel localEx : session.getExercises()) {
                if (!containsExercise(finalExercises, localEx.getExerciseName())) {
                    finalExercises.add(convertExerciseToMap(localEx));
                }
            }

            Map<String, Object> workoutMap = new HashMap<>();
            workoutMap.put("date", session.getWorkoutDate());
            workoutMap.put("title", session.getWorkoutTitle() != null ? session.getWorkoutTitle() : "Тренировка");
            workoutMap.put("exercises", finalExercises);

            docRef.set(workoutMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Облако обновлено для " + session.getWorkoutDate()));
        });
    }

    /**
     * Сохранение из облака на телефон: Только дописывает то, чего нет.
     */
    private void saveCloudSessionToSQLite(WorkoutSessionModel session) {
        if (session == null || session.getExercises() == null) return;
        WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());

        for (ExerciseModel cloudEx : session.getExercises()) {
            // ПРОВЕРКА: Если такого упражнения на эту дату нет локально - добавляем
            if (!dao.isExerciseExists(cloudEx.getExerciseName(), session.getWorkoutDate())) {
                if (cloudEx.getEx_Data() == null || cloudEx.getEx_Data().isEmpty()) {
                    cloudEx.setEx_Data(session.getWorkoutDate());
                }
                dao.addFullExerciseFromCloud(cloudEx);
                Log.d(TAG, "Добавлено новое упражнение из облака: " + cloudEx.getExerciseName());
            }
        }
    }

    // --- Вспомогательные методы ---

    private boolean containsExercise(List<Map<String, Object>> list, String name) {
        for (Map<String, Object> item : list) {
            if (name.equals(item.get("exerciseName"))) return true;
        }
        return false;
    }

    private Map<String, List<ExerciseModel>> groupExercisesByDate(List<ExerciseModel> exercises) {
        Map<String, List<ExerciseModel>> map = new HashMap<>();
        for (ExerciseModel ex : exercises) {
            String date = ex.getEx_Data();
            if (date != null && !date.isEmpty()) {
                map.computeIfAbsent(date, k -> new ArrayList<>()).add(ex);
            }
        }
        return map;
    }

    private Map<String, Object> convertExerciseToMap(ExerciseModel ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("exerciseName", ex.getExerciseName());
        map.put("exerciseType", ex.getExerciseType());
        map.put("exerciseBodyType", ex.getExerciseBodyType());
        map.put("state", ex.getState());

        List<Map<String, Object>> setsList = new ArrayList<>();
        if (ex.getSets() != null) {
            for (Object set : ex.getSets()) {
                if (set instanceof StrengthSetModel) setsList.add(convertStrengthSet((StrengthSetModel) set));
                else if (set instanceof CardioSetModel) setsList.add(convertCardioSet((CardioSetModel) set));
            }
        }
        map.put("sets", setsList);
        return map;
    }

    private Map<String, Object> convertStrengthSet(StrengthSetModel s) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "strength");
        m.put("weight", s.getWeight());
        m.put("rep", s.getRep());
        m.put("order", s.getOrder());
        m.put("state", s.getState());
        return m;
    }

    private Map<String, Object> convertCardioSet(CardioSetModel c) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "cardio");
        m.put("temp", c.getTemp());
        m.put("time", c.getTime());
        m.put("distance", c.getDistance());
        m.put("order", c.getOrder());
        m.put("state", c.getState());
        return m;
    }
}