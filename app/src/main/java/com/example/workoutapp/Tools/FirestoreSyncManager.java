package com.example.workoutapp.Tools;

import android.util.Log;

import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.Models.WorkoutModels.WorkoutSessionModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreSyncManager {

    private final FirebaseFirestore db;
    private final String userId;

    public FirestoreSyncManager() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    /**
     * ГЛАВНЫЙ МЕТОД: Группирует плоский список упражнений из SQLite по датам
     * и отправляет в Firestore как отдельные сессии.
     */
    public void syncAllWorkouts(List<ExerciseModel> allExercises) {
        if (userId == null || allExercises == null || allExercises.isEmpty()) {
            Log.d("FirestoreSync", "Синхронизация отменена: юзер не авторизован или данных нет");
            return;
        }

        // 1. Группируем упражнения по датам (ex_Data)
        Map<String, List<ExerciseModel>> groupedWorkouts = new HashMap<>();

        for (ExerciseModel ex : allExercises) {
            String date = ex.getEx_Data();
            if (date == null || date.isEmpty()) continue;

            if (!groupedWorkouts.containsKey(date)) {
                groupedWorkouts.put(date, new ArrayList<>());
            }
            groupedWorkouts.get(date).add(ex);
        }

        // 2. Итерируемся по сгруппированным данным и загружаем каждую дату
        for (Map.Entry<String, List<ExerciseModel>> entry : groupedWorkouts.entrySet()) {
            String date = entry.getKey();
            List<ExerciseModel> exercisesForDay = entry.getValue();

            // Создаем модель сессии (она вычислит заголовок типа "Силовая тренировка")
            WorkoutSessionModel session = new WorkoutSessionModel(date, exercisesForDay);

            // Вызываем метод загрузки одного документа
            uploadWorkoutSession(session);
        }
    }

    /**
     * Загружает одну сессию (один день) в Firestore
     */
    public void uploadWorkoutSession(WorkoutSessionModel session) {
        if (userId == null) return;

        DocumentReference docRef = db.collection("users").document(userId)
                .collection("workouts").document(session.getWorkoutDate());

        // 1. Сначала читаем, что уже есть в облаке
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            List<Map<String, Object>> finalExercisesData = new ArrayList<>();

            if (documentSnapshot.exists()) {
                // Если день уже есть, берем существующие упражнения
                List<Map<String, Object>> existingExercises = (List<Map<String, Object>>) documentSnapshot.get("exercises");
                if (existingExercises != null) {
                    finalExercisesData.addAll(existingExercises);
                }
            }

            // 2. Добавляем новые упражнения, которых еще нет в списке (проверка по имени или ID)
            for (ExerciseModel newEx : session.getExercises()) {
                Map<String, Object> newExMap = convertExerciseToMap(newEx);

                // Простейшая проверка на дубликаты (чтобы не добавить одно и то же при повторном нажатии)
                if (!containsExercise(finalExercisesData, newEx.getExerciseName())) {
                    finalExercisesData.add(newExMap);
                }
            }

            // 3. Отправляем обновленный полный список
            Map<String, Object> workoutMap = new HashMap<>();
            workoutMap.put("date", session.getWorkoutDate());
            workoutMap.put("title", session.getWorkoutTitle());
            workoutMap.put("exercises", finalExercisesData);

            docRef.set(workoutMap, SetOptions.merge());
        });
    }

    // Вспомогательный метод для проверки дубликатов
    private boolean containsExercise(List<Map<String, Object>> list, String name) {
        for (Map<String, Object> item : list) {
            if (name.equals(item.get("exerciseName"))) return true;
        }
        return false;
    }

    private Map<String, Object> convertExerciseToMap(ExerciseModel ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("exerciseName", ex.getExerciseName());
        map.put("exerciseType", ex.getExerciseType());
        map.put("exerciseBodyType", ex.getExerciseBodyType());
        map.put("state", ex.getState());

        List<Map<String, Object>> setsList = new ArrayList<>();
        for (Object set : ex.getSets()) {
            if (set instanceof StrengthSetModel) {
                setsList.add(convertStrengthSet((StrengthSetModel) set));
            } else if (set instanceof CardioSetModel) {
                setsList.add(convertCardioSet((CardioSetModel) set));
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

    public void startFullSynchronization(List<ExerciseModel> localExercises) {
        if (userId == null) return;

        db.collection("users").document(userId).collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, DocumentSnapshot> cloudMap = new HashMap<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        cloudMap.put(doc.getId(), doc);
                    }

                    Map<String, List<ExerciseModel>> localGrouped = groupExercisesByDate(localExercises);

                    // 1. СИНХРОНИЗАЦИЯ С ТЕЛЕФОНА В ОБЛАКО
                    for (String date : localGrouped.keySet()) {
                        // Мы ВСЕГДА вызываем upload, а внутри него уже решаем:
                        // добавить только новые упражнения или создать новую сессию.
                        WorkoutSessionModel session = new WorkoutSessionModel(date, localGrouped.get(date));
                        uploadWorkoutSession(session);
                    }

                    // 2. СИНХРОНИЗАЦИЯ ИЗ ОБЛАКА В ТЕЛЕФОН
                    for (String cloudDate : cloudMap.keySet()) {
                        if (!localGrouped.containsKey(cloudDate)) {
                            // Если на телефоне вообще нет этой даты — скачиваем целиком
                            WorkoutSessionModel cloudSession = cloudMap.get(cloudDate).toObject(WorkoutSessionModel.class);
                            saveCloudSessionToSQLite(cloudSession);
                        } else {
                            // ТУТ СЛОЖНЕЕ: Дата есть и там, и там.
                            // Нужно проверить, есть ли в облаке упражнения, которых нет на телефоне.
                            WorkoutSessionModel cloudSession = cloudMap.get(cloudDate).toObject(WorkoutSessionModel.class);
                            syncMissingExercisesToLocal(cloudSession, localGrouped.get(cloudDate));
                        }
                    }
                });
    }
    private void syncMissingExercisesToLocal(WorkoutSessionModel cloudSession, List<ExerciseModel> localExercises) {
        if (cloudSession == null || cloudSession.getExercises() == null) return;

        net.sqlcipher.database.SQLiteDatabase db = com.example.workoutapp.MainActivity.getAppDataBase();
        com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO dao =
                new com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO(db);

        for (ExerciseModel cloudEx : cloudSession.getExercises()) {
            boolean existsLocally = false;
            for (ExerciseModel localEx : localExercises) {
                if (cloudEx.getExerciseName().equals(localEx.getExerciseName())) {
                    existsLocally = true;
                    break;
                }
            }

            if (!existsLocally) {
                if (cloudEx.getEx_Data() == null || cloudEx.getEx_Data().isEmpty()) {
                    cloudEx.setEx_Data(cloudSession.getWorkoutDate());
                }
                dao.addFullExerciseFromCloud(cloudEx);
                Log.d("FirestoreSync", "Добавлено недостающее упражнение локально: " + cloudEx.getExerciseName());
            }
        }
    }

    private void saveCloudSessionToSQLite(WorkoutSessionModel session) {
        if (session == null || session.getExercises() == null) return;

        // 1. Получаем базу данных (через ваш статический метод в MainActivity)
        net.sqlcipher.database.SQLiteDatabase db = com.example.workoutapp.MainActivity.getAppDataBase();

        // 2. Инициализируем DAO упражнений
        com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO dao =
                new com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO(db);

        // 3. Проходим по упражнениям из сессии
        for (ExerciseModel ex : session.getExercises()) {
            // Гарантируем, что дата проставлена (берем из ID документа/сессии)
            if (ex.getEx_Data() == null || ex.getEx_Data().isEmpty()) {
                ex.setEx_Data(session.getWorkoutDate());
            }

            // 4. Сохраняем в SQLite через созданный нами ранее метод
            dao.addFullExerciseFromCloud(ex);
        }
        Log.d("FirestoreSync", "Данные за дату " + session.getWorkoutDate() + " успешно восстановлены из облака.");
    }

    private Map<String, List<ExerciseModel>> groupExercisesByDate(List<ExerciseModel> exercises) {
        Map<String, List<ExerciseModel>> map = new HashMap<>();
        for (ExerciseModel ex : exercises) {
            String date = ex.getEx_Data();
            if (date == null || date.isEmpty()) continue;
            if (!map.containsKey(date)) {
                map.put(date, new ArrayList<>());
            }
            map.get(date).add(ex);
        }
        return map;
    }
}