package com.example.workoutapp.Tools;

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

    public WorkoutSessionSync() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    /**
     * Основной метод синхронизации тренировок
     */
    public void startWorkoutSync(List<ExerciseModel> localExercises, Map<String, com.google.firebase.firestore.DocumentSnapshot> cloudMap) {
        if (userId == null) return;

        Map<String, List<ExerciseModel>> localGrouped = groupExercisesByDate(localExercises);

        // 1. Отправка/обновление локальных данных в облако
        for (String date : localGrouped.keySet()) {
            WorkoutSessionModel session = new WorkoutSessionModel(date, localGrouped.get(date));
            uploadWorkoutSession(session);
        }

        // 2. Проверка данных из облака, которых нет в телефоне (или неполных)
        for (String cloudDate : cloudMap.keySet()) {
            WorkoutSessionModel cloudSession = cloudMap.get(cloudDate).toObject(WorkoutSessionModel.class);
            if (cloudSession == null) continue;

            if (!localGrouped.containsKey(cloudDate)) {
                // Если даты на телефоне вообще нет - скачиваем всё
                saveCloudSessionToSQLite(cloudSession);
            } else {
                // Если дата есть, проверяем на наличие недостающих упражнений
                syncMissingExercisesToLocal(cloudSession, localGrouped.get(cloudDate));
            }
        }
    }

    /**
     * Загрузка одной сессии в Firestore слиянием (merge) упражнений
     */
    public void uploadWorkoutSession(WorkoutSessionModel session) {
        if (userId == null) return;

        DocumentReference docRef = db.collection("users").document(userId)
                .collection("workouts").document(session.getWorkoutDate());

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            List<Map<String, Object>> finalExercisesData = new ArrayList<>();

            if (documentSnapshot.exists()) {
                List<Map<String, Object>> existing = (List<Map<String, Object>>) documentSnapshot.get("exercises");
                if (existing != null) finalExercisesData.addAll(existing);
            }

            for (ExerciseModel newEx : session.getExercises()) {
                if (!containsExercise(finalExercisesData, newEx.getExerciseName())) {
                    finalExercisesData.add(convertExerciseToMap(newEx));
                }
            }

            Map<String, Object> workoutMap = new HashMap<>();
            workoutMap.put("date", session.getWorkoutDate());
            workoutMap.put("title", session.getWorkoutTitle());
            workoutMap.put("exercises", finalExercisesData);

            docRef.set(workoutMap, SetOptions.merge());
        });
    }

    /**
     * Сохранение сессии из облака в локальную БД
     */
    private void saveCloudSessionToSQLite(WorkoutSessionModel session) {
        if (session == null || session.getExercises() == null) return;
        WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());

        for (ExerciseModel ex : session.getExercises()) {
            if (ex.getEx_Data() == null || ex.getEx_Data().isEmpty()) {
                ex.setEx_Data(session.getWorkoutDate());
            }
            dao.addFullExerciseFromCloud(ex);
        }
    }

    /**
     * Докачка упражнений, которых нет локально за конкретный день
     */
    private void syncMissingExercisesToLocal(WorkoutSessionModel cloudSession, List<ExerciseModel> localExercises) {
        WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());

        for (ExerciseModel cloudEx : cloudSession.getExercises()) {
            boolean existsLocally = localExercises.stream()
                    .anyMatch(l -> l.getExerciseName().equals(cloudEx.getExerciseName()));

            if (!existsLocally) {
                cloudEx.setEx_Data(cloudSession.getWorkoutDate());
                dao.addFullExerciseFromCloud(cloudEx);
            }
        }
    }

    // --- Вспомогательные методы конвертации ---

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

    private boolean containsExercise(List<Map<String, Object>> list, String name) {
        for (Map<String, Object> item : list) {
            if (name.equals(item.get("exerciseName"))) return true;
        }
        return false;
    }

    /**
     * Группирует плоский список упражнений из SQLite по датам
     * и отправляет в Firestore как отдельные сессии.
     */
    public void syncAllWorkouts(List<ExerciseModel> allExercises) {
        if (userId == null || allExercises == null || allExercises.isEmpty()) {
            return;
        }

        // Группируем упражнения по датам (ex_Data)
        Map<String, List<ExerciseModel>> groupedWorkouts = new HashMap<>();
        for (ExerciseModel ex : allExercises) {
            String date = ex.getEx_Data();
            if (date == null || date.isEmpty()) continue;
            groupedWorkouts.computeIfAbsent(date, k -> new ArrayList<>()).add(ex);
        }

        // Итерируемся и загружаем каждую дату
        for (Map.Entry<String, List<ExerciseModel>> entry : groupedWorkouts.entrySet()) {
            WorkoutSessionModel session = new WorkoutSessionModel(entry.getKey(), entry.getValue());
            uploadWorkoutSession(session);
        }
    }
}