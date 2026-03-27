package com.example.workoutapp.Tools.SyncTools;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.Helpers.WorkoutSessionModel;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WorkoutSessionSync2 {

    private final FirebaseFirestore db;
    private final String userId;
    private static final String TAG = "WorkoutSessionSync";
    public interface SyncCallback {
        void onSuccess();
        void onFailure(String error);
    }


    public WorkoutSessionSync2() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();
    }

    public interface DownloadCallback {
        void onDownloaded(int sessionCount);
        void onError(String error);
    }

    /**
     * Точка входа в синхронизацию тренировок
     */
    public void startWorkoutSync(List<ExerciseModel> localExercises) {
        if (userId == null) {
            Log.e(TAG, "Sync aborted: userId is null");
            return;
        }

        // 1. Отправляем локальные данные (синхронизация "туда")
        syncAllWorkouts(localExercises);

        // 2. Получаем данные (синхронизация "оттуда")
        db.collection("users").document(userId).collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "В облаке нет документов для синхронизации.");
                        return;
                    }

                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        String cloudDate = snapshot.getId();
                        Log.d(TAG, "--- Обработка документа: " + cloudDate + " ---");

                        // Печатаем вообще всё, что есть в документе, чтобы поймать структуру
                        Map<String, Object> allData = snapshot.getData();
                        Log.d(TAG, "Полные данные документа: " + allData);

                        Object exercisesObj = snapshot.get("exercises_map");

                        if (exercisesObj instanceof Map) {
                            Map<String, Object> exercisesMap = (Map<String, Object>) exercisesObj;
                            Log.d(TAG, "Найдено упражнений в exercises_map: " + exercisesMap.size());

                            List<ExerciseModel> cloudExercises = new ArrayList<>();
                            for (Object value : exercisesMap.values()) {
                                if (value instanceof Map) {
                                    cloudExercises.add(mapToExercise((Map<String, Object>) value));
                                }
                            }

                            WorkoutSessionModel cloudSession = new WorkoutSessionModel();
                            cloudSession.setWorkoutDate(cloudDate);
                            cloudSession.setExercises(cloudExercises);

                            // Сохраняем в локальную БД
                            processAndSaveCloudSession(cloudSession);
                        } else {
                            Log.w(TAG, "Поле exercises_map не является объектом или отсутствует в " + cloudDate);
                        }
                    }
                    Log.d(TAG, "Синхронизация из облака полностью завершена.");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка Firestore: " + e.getMessage()));
    }

    /**
     * Отправляет список конкретных упражнений на сервер.
     * Автоматически группирует их по датам, чтобы не делать лишних запросов.
     */
    public void syncSpecificExercises(List<ExerciseModel> exercises, @Nullable WorkoutSessionSync2.SyncCallback callback) {
        if (userId == null || exercises == null || exercises.isEmpty()) {
            if (callback != null) callback.onFailure("No data");
            return;
        }

        Map<String, List<ExerciseModel>> groupedByDate = groupExercisesByDate(exercises);

        // Счетчик для отслеживания завершения всех запросов
        int totalDates = groupedByDate.size();
        java.util.concurrent.atomic.AtomicInteger completedDates = new java.util.concurrent.atomic.AtomicInteger(0);

        for (Map.Entry<String, List<ExerciseModel>> entry : groupedByDate.entrySet()) {
            String date = entry.getKey();
            DocumentReference docRef = db.collection("users").document(userId)
                    .collection("workouts").document(date);

            Map<String, Object> nestedExercises = new HashMap<>();
            for (ExerciseModel ex : entry.getValue()) {
                if (ex.getExercise_uid() != null) {
                    nestedExercises.put(ex.getExercise_uid(), convertExerciseToMap(ex));
                }
            }

            Map<String, Object> finalData = new HashMap<>();
            finalData.put("workoutDate", date);
            finalData.put("exercises_map", nestedExercises);

            docRef.set(finalData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Данные упражнений синхронизированы для даты: " + date);

                        // Когда ВСЕ даты из списка обработаны, вызываем успех
                        if (completedDates.incrementAndGet() == totalDates) {
                            if (callback != null) {
                                callback.onSuccess(); // ВОТ ЭТОТ ВЫЗОВ УДАЛИТ UID ИЗ ТАБЛИЦЫ
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Ошибка при синхронизации даты " + date + ": " + e.getMessage());
                        if (callback != null) {
                            callback.onFailure(e.getMessage());
                        }
                    });
        }
    }


    /**
     * Логика проверки старых упражнений и синхронизации с базой
     */
    private void processAndSaveCloudSession(WorkoutSessionModel session) {
        if (session == null || session.getExercises() == null) return;

        WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (ExerciseModel cloudEx : session.getExercises()) {
            String uid = cloudEx.getExercise_uid();
            if (uid == null || uid.isEmpty()) continue;

            // --- 1. ФИЛЬТРАЦИЯ ПОДХОДОВ ---
            // Мы оставляем все подходы, чтобы не потерять черновики пользователя.
            // Если ты всё же хочешь фильтровать совсем "битые" данные,
            // убедись, что isSetEmpty не слишком строгий.
            List<Object> currentSets = cloudEx.getSets();
            if (currentSets == null) {
                currentSets = new ArrayList<>();
            }

            try {
                // Парсим дату сессии
                String sessionDateStr = session.getWorkoutDate();
                Date workoutDate = sdf.parse(sessionDateStr);
                long diffInMs = now - workoutDate.getTime();
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs);

                // --- 2. ПРАВИЛО ЗАВЕРШЕНИЯ СТАРЫХ ТРЕНИРОВОК ---
                // Если тренировке больше 2 дней и она не завершена — закрываем её.
                if (diffInDays >= 2) {
                    if (!"finished".equals(cloudEx.getState())) {
                        cloudEx.setState("finished");
                        // Обновляем статус в облаке (без колбэка, так как это фоновая правка)
                        updateExerciseSetsInCloud(cloudEx);
                    }
                }

                // Гарантируем наличие даты в модели упражнения
                if (cloudEx.getEx_Data() == null) {
                    cloudEx.setEx_Data(sessionDateStr);
                }

                // --- 3. СОХРАНЕНИЕ В ЛОКАЛЬНУЮ БД ---
                // Проверяем по LAST_MODIFIED (если в DAO есть такая логика) или просто обновляем.
                if (dao.isExerciseUidExists(uid)) {
                    Log.d(TAG, "Обновление упражнения локально: " + cloudEx.getExerciseName());
                    dao.updateFullExerciseFromCloud(cloudEx);
                } else {
                    Log.d(TAG, "Добавление нового упражнения из облака: " + cloudEx.getExerciseName());
                    dao.addFullExerciseFromCloud(cloudEx);
                }

            } catch (Exception e) {
                Log.e(TAG, "Ошибка при обработке упражнения " + uid + ": " + e.getMessage());
            }
        }
    }

    /**
     * Вспомогательный метод для проверки конкретного подхода.
     * Возвращает true, если в подходе одни нули.
     */
//    private boolean isSetEmpty(Object setObj) {
//        if (setObj instanceof StrengthSetModel) {
//            StrengthSetModel s = (StrengthSetModel) setObj;
//            return s.getStrength_set_weight() <= 0 && s.getStrength_set_rep() <= 0;
//        } else if (setObj instanceof CardioSetModel) {
//            CardioSetModel c = (CardioSetModel) setObj;
//            return c.getCardio_set_distance() <= 0 && c.getCardio_set_time() <= 0;
//        }
//        return true;
//    }

    public void syncAllWorkouts(List<ExerciseModel> allExercises) {
        if (userId == null || allExercises == null || allExercises.isEmpty()) return;
        Map<String, List<ExerciseModel>> grouped = groupExercisesByDate(allExercises);
        for (String date : grouped.keySet()) {
            uploadWorkoutSession(new WorkoutSessionModel(date, grouped.get(date)));
        }
    }



    private Map<String, List<ExerciseModel>> groupExercisesByDate(List<ExerciseModel> exercises) {
        Map<String, List<ExerciseModel>> map = new HashMap<>();
        for (ExerciseModel ex : exercises) {
            String date = ex.getEx_Data();
            if (date != null) {
                if (!map.containsKey(date)) map.put(date, new ArrayList<>());
                map.get(date).add(ex);
            }
        }
        return map;
    }

    public void uploadWorkoutSession(WorkoutSessionModel session) {
        if (userId == null || session == null || session.getWorkoutDate() == null) return;
        DocumentReference docRef = db.collection("users").document(userId)
                .collection("workouts").document(session.getWorkoutDate());

        Map<String, Object> exercisesMap = new HashMap<>();
        for (ExerciseModel ex : session.getExercises()) {
            exercisesMap.put(ex.getExercise_uid(), convertExerciseToMap(ex));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("workoutDate", session.getWorkoutDate());
        data.put("workoutTitle", "Workout:" + session.getWorkoutDate());
        data.put("exercises_map", exercisesMap);

        docRef.set(data, SetOptions.merge());
    }

    public void updateExerciseSetsInCloud(ExerciseModel exercise) {
        if (userId == null || exercise == null || exercise.getEx_Data() == null) return;
        String uid = exercise.getExercise_uid();
        DocumentReference docRef = db.collection("users").document(userId)
                .collection("workouts").document(exercise.getEx_Data());

        Map<String, Object> updates = new HashMap<>();
        updates.put("exercises_map." + uid, convertExerciseToMap(exercise));

        docRef.update(updates).addOnSuccessListener(aVoid -> Log.d(TAG, "Упражнение " + uid + " обновлено в облаке."));
    }

    public void removeExerciseFromCloud(ExerciseModel exercise, @Nullable WorkoutSessionSync2.SyncCallback callback) {
        if (userId == null || exercise == null || exercise.getEx_Data() == null) {
            if (callback != null) callback.onFailure("Missing data");
            return;
        }
        String uid = exercise.getExercise_uid();
        DocumentReference docRef = db.collection("users").document(userId)
                .collection("workouts").document(exercise.getEx_Data());

        Map<String, Object> updates = new HashMap<>();
        updates.put("exercises_map." + uid, FieldValue.delete());

        docRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Удалено из облака: " + uid);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    private Map<String, Object> convertExerciseToMap(ExerciseModel ex) {
        Map<String, Object> map = new HashMap<>();
        map.put("exercise_uid", ex.getExercise_uid());
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
                    setsList.add(convertStrengthSet((StrengthSetModel) set));
                } else if (set instanceof CardioSetModel) {
                    setsList.add(convertCardioSet((CardioSetModel) set));
                }
            }
        }
        map.put("sets", setsList);
        return map;
    }

    private Map<String, Object> convertStrengthSet(StrengthSetModel s) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "strength");
        m.put("strength_set_weight", s.getStrength_set_weight());
        m.put("strength_set_rep", s.getStrength_set_rep());
        m.put("strength_set_order", s.getStrength_set_order());
        m.put("strength_set_state", s.getStrength_set_state());
        return m;
    }

    private Map<String, Object> convertCardioSet(CardioSetModel c) {
        Map<String, Object> m = new HashMap<>();
        m.put("type", "cardio");
        m.put("cardio_set_time", c.getCardio_set_time());
        m.put("cardio_set_distance", c.getCardio_set_distance());
        m.put("cardio_set_order", c.getCardio_set_order());
        m.put("cardio_set_state", c.getCardio_set_state());
        return m;
    }

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
     * Загрузка всех тренировок из облака при первом входе.
     */
    public void downloadAllWorkouts(@Nullable DownloadCallback callback) {
        if (userId == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        db.collection("users").document(userId).collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null) {
                        if (callback != null) callback.onDownloaded(0);
                        return;
                    }

                    int count = 0;
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        String cloudDate = snapshot.getId();
                        Object exercisesObj = snapshot.get("exercises_map");

                        if (exercisesObj instanceof Map) {
                            Map<String, Object> exercisesMap = (Map<String, Object>) exercisesObj;
                            List<ExerciseModel> cloudExercises = new ArrayList<>();

                            for (Object value : exercisesMap.values()) {
                                if (value instanceof Map) {
                                    cloudExercises.add(mapToExercise((Map<String, Object>) value));
                                }
                            }

                            WorkoutSessionModel cloudSession = new WorkoutSessionModel();
                            cloudSession.setWorkoutDate(cloudDate);
                            cloudSession.setExercises(cloudExercises);

                            // Используем твой существующий метод сохранения в БД
                            processAndSaveCloudSession(cloudSession);
                            count++;
                        }
                    }

                    Log.d(TAG, "Синхронизация тренировок завершена. Загружено сессий: " + count);
                    if (callback != null) callback.onDownloaded(count);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка загрузки тренировок: " + e.getMessage());
                    if (callback != null) callback.onError(e.getMessage());
                });
    }


}