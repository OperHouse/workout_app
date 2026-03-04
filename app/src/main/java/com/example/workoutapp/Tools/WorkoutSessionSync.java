package com.example.workoutapp.Tools;

import android.util.Log;

import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.Helpers.WorkoutSessionModel;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.*;

public class WorkoutSessionSync {

    private final FirebaseFirestore db;
    private final String userId;
    private static final String TAG = "WorkoutSessionSync";

    public WorkoutSessionSync() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

//    /**
//     * Полная синхронизация
//     */
//    public void startWorkoutSync(List<ExerciseModel> localExercises,
//                                 Map<String, DocumentSnapshot> cloudMap) {
//
//        if (userId == null) return;
//
//        // 1. Сначала отправляем локальные данные в облако (полная замена массива)
//        syncAllWorkouts(localExercises);
//
//        // 2. Потом подтягиваем данные из облака и обновляем локальную БД
//        if (cloudMap == null) return;
//
//        for (String cloudDate : cloudMap.keySet()) {
//            DocumentSnapshot snapshot = cloudMap.get(cloudDate);
//            if (snapshot != null && snapshot.exists()) {
//
//                WorkoutSessionModel cloudSession =
//                        snapshot.toObject(WorkoutSessionModel.class);
//
//                if (cloudSession != null) {
//
//                    if (cloudSession.getWorkoutDate() == null) {
//                        cloudSession.setWorkoutDate(cloudDate);
//                    }
//
//                    saveOrUpdateCloudSessionToSQLite(cloudSession);
//                }
//            }
//        }
//    }

    /**
     * Группировка и отправка по датам
     */
    public void syncAllWorkouts(List<ExerciseModel> allExercises) {

        if (userId == null || allExercises == null || allExercises.isEmpty())
            return;

        Map<String, List<ExerciseModel>> grouped =
                groupExercisesByDate(allExercises);

        for (String date : grouped.keySet()) {

            WorkoutSessionModel session =
                    new WorkoutSessionModel(date, grouped.get(date));

            uploadWorkoutSession(session);
        }
    }

    /**
     * Полная перезапись массива exercises
     */
    public void uploadWorkoutSession(WorkoutSessionModel session) {
        if (userId == null || session == null || session.getWorkoutDate() == null) return;

        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("workouts")
                .document(session.getWorkoutDate());

        // Вместо списка создаем карту, где ключ — UID упражнения
        Map<String, Object> exercisesMap = new HashMap<>();

        for (ExerciseModel ex : session.getExercises()) {
            String uid = ex.getExercise_uid();
            if (uid != null && !uid.isEmpty()) {
                // Кладем упражнение в мапу под его уникальным ключом
                // Мы используем вложенный путь "exercises.UID", чтобы обновить только его
                exercisesMap.put(uid, convertExerciseToMap(ex));
            }
        }

        // Подготавливаем финальный объект для отправки
        Map<String, Object> data = new HashMap<>();
        data.put("workoutDate", session.getWorkoutDate());
        data.put("workoutTitle", session.getWorkoutTitle() != null ? session.getWorkoutTitle() : "Тренировка");
        // ВАЖНО: записываем карту в поле "exercises_map"
        data.put("exercises_map", exercisesMap);

        // МГНОВЕННЫЙ ВЫЗОВ (без get!)
        // Merge гарантирует, что старые упражнения в exercises_map,
        // которых нет в текущем exercisesMap, НЕ БУДУТ удалены.
        docRef.set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Синхронизация (merge) успешна"))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка: " + e.getMessage()));
    }

    /**
     * Сохранение или обновление из облака
     */
    private void saveOrUpdateCloudSessionToSQLite(
            WorkoutSessionModel session) {

        if (session == null || session.getExercises() == null)
            return;

        WORKOUT_EXERCISE_TABLE_DAO dao =
                new WORKOUT_EXERCISE_TABLE_DAO(
                        MainActivity.getAppDataBase());

        for (ExerciseModel cloudEx : session.getExercises()) {

            String uid = cloudEx.getExercise_uid();
            if (uid == null || uid.isEmpty()) continue;

            if (cloudEx.getEx_Data() == null
                    || cloudEx.getEx_Data().isEmpty()) {
                cloudEx.setEx_Data(session.getWorkoutDate());
            }

            if (dao.isExerciseUidExists(uid)) {
                dao.updateFullExerciseFromCloud(cloudEx);
                Log.d(TAG, "Обновлено из облака: "
                        + cloudEx.getExerciseName());
            } else {
                dao.addFullExerciseFromCloud(cloudEx);
                Log.d(TAG, "Добавлено из облака: "
                        + cloudEx.getExerciseName());
            }
        }
    }

    private Map<String, List<ExerciseModel>> groupExercisesByDate(
            List<ExerciseModel> exercises) {

        Map<String, List<ExerciseModel>> map = new HashMap<>();

        for (ExerciseModel ex : exercises) {

            String date = ex.getEx_Data();

            if (date != null && !date.isEmpty()) {
                map.computeIfAbsent(date,
                        k -> new ArrayList<>()).add(ex);
            }
        }
        return map;
    }

    private Map<String, Object> convertExerciseToMap(ExerciseModel ex) {

        String uid = ex.getExercise_uid();
        if (uid == null || uid.isEmpty()) {
            uid = UUID.randomUUID().toString();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("exercise_uid", uid);
        map.put("exerciseName", ex.getExerciseName());
        map.put("exerciseType", ex.getExerciseType());
        map.put("exerciseBodyType", ex.getExerciseBodyType());
        map.put("ex_Data", ex.getEx_Data());
        map.put("state", ex.getState());
        map.put("lastModified", ex.getExercise_time_lastModified());

        List<Map<String, Object>> setsList = new ArrayList<>();

        if (ex.getSets() != null) {

            for (Object set : ex.getSets()) {

                if (set instanceof StrengthSetModel) {
                    setsList.add(convertStrengthSet(
                            (StrengthSetModel) set));
                } else if (set instanceof CardioSetModel) {
                    setsList.add(convertCardioSet(
                            (CardioSetModel) set));
                }
            }
        }

        map.put("sets", setsList);

        return map;
    }

    private Map<String, Object> convertStrengthSet(
            StrengthSetModel s) {

        Map<String, Object> m = new HashMap<>();
        m.put("type", "strength");
        m.put("strength_set_weight",
                s.getStrength_set_weight());
        m.put("strength_set_rep",
                s.getStrength_set_rep());
        m.put("strength_set_order",
                s.getStrength_set_order());
        m.put("strength_set_state",
                s.getStrength_set_state());
        return m;
    }

    private Map<String, Object> convertCardioSet(
            CardioSetModel c) {

        Map<String, Object> m = new HashMap<>();
        m.put("type", "cardio");
        m.put("cardio_set_temp",
                c.getCardio_set_temp());
        m.put("cardio_set_time",
                c.getCardio_set_time());
        m.put("cardio_set_distance",
                c.getCardio_set_distance());
        m.put("cardio_set_order",
                c.getCardio_set_order());
        m.put("cardio_set_state",
                c.getCardio_set_state());
        return m;
    }

    /**
     * Удаление упражнения
     */
    public void removeExerciseFromCloud(ExerciseModel exercise) {
        if (userId == null || exercise == null || exercise.getEx_Data() == null) return;

        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("workouts")
                .document(exercise.getEx_Data());

        String uid = exercise.getExercise_uid();
        if (uid == null || uid.isEmpty()) return;

        // Путь к полю внутри мапы пишется через точку
        Map<String, Object> updates = new HashMap<>();
        updates.put(uid, com.google.firebase.firestore.FieldValue.delete());

        docRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Упражнение удалено из облачной карты"))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка удаления ключа: " + e.getMessage()));
    }

    /**
     * Полная синхронизация (Исправлено чтение для Map)
     */
    public void startWorkoutSync(List<ExerciseModel> localExercises,
                                 Map<String, DocumentSnapshot> cloudMap) {

        if (userId == null) return;

        // 1. Отправляем локальное в облако
        syncAllWorkouts(localExercises);

        // 2. Вытягиваем из облака
        if (cloudMap == null) return;

        for (String cloudDate : cloudMap.keySet()) {
            DocumentSnapshot snapshot = cloudMap.get(cloudDate);
            if (snapshot != null && snapshot.exists()) {

                // ВАЖНО: Достаем именно мапу
                Map<String, Object> exercisesMap = (Map<String, Object>) snapshot.get("exercises_map");

                if (exercisesMap != null) {
                    List<ExerciseModel> cloudExercises = new ArrayList<>();

                    for (Object value : exercisesMap.values()) {
                        if (value instanceof Map) {
                            cloudExercises.add(mapToExercise((Map<String, Object>) value));
                        }
                    }

                    // Создаем временную модель сессии для передачи в существующий метод сохранения
                    WorkoutSessionModel cloudSession = new WorkoutSessionModel();
                    cloudSession.setWorkoutDate(cloudDate);
                    cloudSession.setExercises(cloudExercises);

                    saveOrUpdateCloudSessionToSQLite(cloudSession);
                }
            }
        }
    }

    /**
     * Ручной парсинг Map обратно в ExerciseModel
     */
    private ExerciseModel mapToExercise(Map<String, Object> data) {
        ExerciseModel ex = new ExerciseModel();
        ex.setExercise_uid((String) data.get("exercise_uid"));
        ex.setExerciseName((String) data.get("exerciseName"));
        ex.setExerciseType((String) data.get("exerciseType"));
        ex.setExerciseBodyType((String) data.get("exerciseBodyType"));
        ex.setEx_Data((String) data.get("ex_Data"));
        ex.setState((String) data.get("state"));

        if (data.containsKey("lastModified")) {
            Object lm = data.get("lastModified");
            if (lm instanceof Number) {
                ex.setExercise_time_lastModified(((Number) lm).longValue());
            }
        }

        // Парсим сеты
        List<Object> setsList = new ArrayList<>();

        List<Map<String, Object>> rawSets = (List<Map<String, Object>>) data.get("sets");
        if (rawSets != null) {
            for (Map<String, Object> sMap : rawSets) {
                String type = (String) sMap.get("type");
                if ("strength".equals(type)) {
                    StrengthSetModel s = new StrengthSetModel();
                    s.setStrength_set_weight(sMap.get("strength_set_weight") != null ? ((Number) sMap.get("strength_set_weight")).doubleValue() : 0.0);
                    s.setStrength_set_rep(sMap.get("strength_set_rep") != null ? ((Number) sMap.get("strength_set_rep")).intValue() : 0);
                    s.setStrength_set_order(sMap.get("strength_set_order") != null ? ((Number) sMap.get("strength_set_order")).intValue() : 0);
                    s.setStrength_set_state((String) sMap.get("strength_set_state"));
                    setsList.add(s); // Добавляем в локальный список
                } else if ("cardio".equals(type)) {
                    CardioSetModel c = new CardioSetModel();
                    c.setCardio_set_temp(sMap.get("cardio_set_temp") != null ? ((Number) sMap.get("cardio_set_temp")).doubleValue() : 0.0);
                    c.setCardio_set_time(sMap.get("cardio_set_time") != null ? ((Number) sMap.get("cardio_set_time")).intValue() : 0);
                    c.setCardio_set_distance(sMap.get("cardio_set_distance") != null ? ((Number) sMap.get("cardio_set_distance")).doubleValue() : 0.0);
                    c.setCardio_set_order(sMap.get("cardio_set_order") != null ? ((Number) sMap.get("cardio_set_order")).intValue() : 0);
                    c.setCardio_set_state((String) sMap.get("cardio_set_state"));
                    setsList.add(c); // Добавляем в локальный список
                }
            }
        }

        // 2. Устанавливаем готовый список целиком. НЕ используй ex.addSet()!
        ex.setSets(setsList);

        return ex;
    }
    /**
     * Обновляет конкретное упражнение (включая все его подходы) в облаке.
     * Вызывается при добавлении, изменении данных или удалении подхода.
     */
    public void updateExerciseSetsInCloud(ExerciseModel exercise) {
        if (userId == null || exercise == null || exercise.getEx_Data() == null) return;

        String uid = exercise.getExercise_uid();
        if (uid == null || uid.isEmpty()) return;

        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("workouts")
                .document(exercise.getEx_Data());

        // Мы обновляем только одно конкретное упражнение в мапе
        Map<String, Object> updates = new HashMap<>();
        updates.put("exercises_map." + uid, convertExerciseToMap(exercise));

        // Используем update, чтобы не затронуть другие поля документа (например, дату или другие упражнения)
        docRef.update(updates)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Подходы упражнения " + exercise.getExerciseName() + " синхронизированы"))
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка синхронизации подходов: " + e.getMessage()));
    }

}