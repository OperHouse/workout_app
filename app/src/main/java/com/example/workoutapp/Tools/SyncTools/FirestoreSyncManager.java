package com.example.workoutapp.Tools.SyncTools;

import android.content.Context;
import android.util.Log;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.DeletionQueueDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealPresetDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Data.NutritionDao.PresetEatDao;
import com.example.workoutapp.Data.NutritionDao.PresetMealNameDao;
import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao;
import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.DeletionTask;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.example.workoutapp.Models.ProfileModels.UserProfileModel;
import com.example.workoutapp.Models.ProfileModels.WeightHistoryModel;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Tools.WeightSync;
import com.example.workoutapp.Tools.WorkoutSessionSync2;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class FirestoreSyncManager {

    private static final String TAG = "FirestoreSyncManager";

    private final FirebaseFirestore db;
    private final String userId;

    private final BaseExerciseSync baseExerciseSync;
    private final ProfileSync profileSync;
    private final PresetWorkoutSync presetWorkoutSync;
    private final WeightSync weightSync;
    private final ActivityGoalSync activityGoalSync;
    private final GeneralGoalSync generalGoalSync;
    private final FoodGoalSync foodGoalSync;
    private final DailyActivitySync dailyActivitySync;
    private final DailyFoodSync dailyFoodSync;
    private final WorkoutSessionSync2 workoutSessionSync2;
    private final BaseFoodSync baseFoodSync;
    private final MealPresetSync mealPresetSync;
    private final MealSync mealSync;

    private ListenerRegistration foodListener;

    private final Context context;
    private ChangeElmDao changeDao;


    public FirestoreSyncManager(Context context) {

        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getUid();

        this.baseExerciseSync = new BaseExerciseSync();
        this.profileSync = new ProfileSync();
        this.weightSync = new WeightSync();
        this.presetWorkoutSync = new PresetWorkoutSync();
        this.activityGoalSync = new ActivityGoalSync();
        this.generalGoalSync = new GeneralGoalSync();
        this.foodGoalSync = new FoodGoalSync();
        this.dailyActivitySync = new DailyActivitySync();
        this.dailyFoodSync = new DailyFoodSync();
        this.workoutSessionSync2 = new WorkoutSessionSync2();
        this.baseFoodSync = new BaseFoodSync();
        this.mealPresetSync = new MealPresetSync();
        this.mealSync = new MealSync();
        this.changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
    }

    // =====================================================
    // FOOD METHODS (НОВАЯ АРХИТЕКТУРА)
    // =====================================================

    public void uploadFood(FoodModel food) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        if (userId == null) return;
        baseFoodSync.uploadFood(food, null);
    }

    public void deleteFood(FoodModel food) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        if (userId == null) return;
        baseFoodSync.deleteFood(food, null);
    }

    /**
     * Запуск realtime синхронизации еды.
     * Вызывать один раз после авторизации.
     */
    public void startFoodRealtimeSync() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }

        if (userId == null) return;

        if (foodListener != null) {
            foodListener.remove();
        }

        foodListener = baseFoodSync.startRealtimeSync(
                new com.example.workoutapp.Data.NutritionDao.BaseEatDao(
                        MainActivity.getAppDataBase()
                )
        );

        Log.d(TAG, "Food realtime sync started");
    }

    public void stopFoodRealtimeSync() {
        if (foodListener != null) {
            foodListener.remove();
            foodListener = null;
        }
    }

    // =====================================================
    // ПОЛНАЯ СИНХРОНИЗАЦИЯ (БЕЗ enableNetwork)
    // =====================================================

    public void startFullSynchronization(List<ExerciseModel> localExercises) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }

        if (userId == null) return;

        Log.d(TAG, "Starting full synchronization...");
        processPendingDeletions();
        processPendingChanges();

        //baseExerciseSync.restoreUserCustomExercises();
        profileSync.syncProfile();
        weightSync.syncWeightHistory();

        syncActivityGoals();
        syncGeneralGoals();
        syncFoodGoals();
        syncDailyActivity();
        syncDailyFood();

        startWorkoutSync(localExercises);

        // ЕДА теперь синкается через realtime listener
        startFoodRealtimeSync();

        Log.d(TAG, "Starting sync of all meal presets...");
        mealPresetSync.downloadAllOnce();

        receiveMealFromServer();


        Log.d(TAG, "Full sync initialized");
    }

    // =====================================================
    // ОСТАЛЬНЫЕ СИНХРОНИЗАЦИИ
    // =====================================================

    public void syncPresetUpdate(String name, String uid, List<ExerciseModel> exercises) {
        if (uid == null) return;

        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());

        // 1. Всегда записываем в очередь изменений
        changeDao.enqueue(uid, "workout_preset");

        // 2. Если интернета нет — выходим, фоновая задача отправит данные позже
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Офлайн. Пресет " + name + " сохранен в очередь изменений.");
            return;
        }

        // 3. Пытаемся отправить сразу (нужно добавить SyncCallback в PresetWorkoutSync)
        presetWorkoutSync.uploadPreset(name, uid, exercises, new PresetWorkoutSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем из очереди при успехе
                changeDao.removeFromQueue(uid);
                Log.d(TAG, "Пресет синхронизирован: " + name);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка синхронизации пресета: " + error);
            }
        });
    }

    public void deletePresetFromCloud(String presetUid) {
        if (presetUid == null) return;

        DeletionQueueDao deletionQueueDao = new DeletionQueueDao(MainActivity.getAppDataBase());
        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());

        // 1. Кладем в очередь удаления
        deletionQueueDao.enqueue(presetUid, "workout_preset_delete", "");

        if (!isNetworkAvailable()) {
            Log.d(TAG, "Офлайн. Запрос на удаление пресета в очереди.");
            return;
        }

        // 2. Сразу удаляем из облака
        presetWorkoutSync.deletePresetFromCloud(presetUid, new PresetWorkoutSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Очищаем обе таблицы
                deletionQueueDao.removeFromQueue(presetUid);
                changeDao.removeFromQueue(presetUid);
                Log.d(TAG, "Пресет удален из облака и очередей.");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка удаления пресета: " + error);
            }
        });
    }

    public void syncBaseExerciseChange(String oldName, BaseExModel updatedEx) {
        if (updatedEx == null || updatedEx.getBase_ex_uid() == null) return;

        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
        final String uid = updatedEx.getBase_ex_uid();

        // 1. Записываем в очередь (используем строгий нижний регистр для типа)
        changeDao.enqueue(uid, "base_exercise");

        if (!isNetworkAvailable()) {
            Log.d(TAG, "Офлайн. Изменение базового упражнения сохранено в очередь.");
            return;
        }

        // 2. Пытаемся отправить (убедись, что в baseExerciseSync добавлен SyncCallback)
        baseExerciseSync.syncBaseExerciseChange(oldName, updatedEx, new BaseExerciseSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем из таблицы change_elm_table только после подтверждения от сервера
                changeDao.removeFromQueue(uid);
                Log.d(TAG, "Библиотека упражнений обновлена: " + uid);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка синхронизации базы: " + error);
            }
        });
    }

    public void deleteBaseExerciseFromCloud(BaseExModel exercise) {
        if (exercise == null || exercise.getBase_ex_uid() == null) {
            Log.e(TAG, "Удаление отменено: UID пуст");
            return;
        }

        final String uid = exercise.getBase_ex_uid();
        DeletionQueueDao deletionQueueDao = new DeletionQueueDao(MainActivity.getAppDataBase());
        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());

        // 1. ЗАПИСЬ В ТАБЛИЦУ (теперь точно запишет)
        // Используем "base_ex_delete"
        deletionQueueDao.enqueue(uid, "base_ex_delete", "");
        Log.d(TAG, "Записано в очередь удаления: " + uid);

        if (!isNetworkAvailable()) return;

        // 2. СРАЗУ УДАЛЯЕМ (вызываем метод удаления, а не изменения!)
        baseExerciseSync.deleteBaseExercise(uid, new BaseExerciseSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Очистка обеих очередей
                deletionQueueDao.removeFromQueue(uid);
                changeDao.removeFromQueue(uid);
                Log.d(TAG, "Успешно удалено из облака и очередей: " + uid);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка удаления в Firebase: " + error);
            }
        });
    }

    public void deleteExerciseFromCloud(ExerciseModel exercise) {
        if (exercise == null || exercise.getExercise_uid() == null) return;

        DeletionQueueDao deletionQueueDao = new DeletionQueueDao(MainActivity.getAppDataBase());
        String exerciseUid = exercise.getExercise_uid();
        String exerciseData = exercise.getEx_Data();

        // 1. Кладем в очередь с пометкой на удаление
        deletionQueueDao.enqueue(exerciseUid, "Workout_ex_delete", exerciseData);

        // 2. Если сети нет — выходим.
        if (!isNetworkAvailable()) {
            Log.d("DEBUG_SYNC", "Нет сети. Запрос на удаление " + exerciseUid + " в очереди.");
            return;
        }

        // 3. Пытаемся удалить в Firebase сразу
        workoutSessionSync2.removeExerciseFromCloud(exercise, new WorkoutSessionSync2.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем задачу из очереди, так как в облаке стерто
                deletionQueueDao.removeFromQueue(exerciseUid);
                Log.d("DEBUG_SYNC", "Упражнение удалено из облака: " + exerciseUid);
            }

            @Override
            public void onFailure(String error) {
                Log.e("DEBUG_SYNC", "Ошибка удаления из облака: " + error);
            }
        });
    }

    public void uploadAllBaseExercises(List<BaseExModel> list, boolean isPublic) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        baseExerciseSync.syncBaseExercises(list, isPublic);
    }

    public void syncProfileUpdate(UserProfileModel profile) {

        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        profileSync.uploadProfile(profile);
    }

    public void syncNewWeight(WeightHistoryModel weightEntry) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        if (weightEntry != null) {
            weightSync.uploadWeightEntry(weightEntry);
        }
    }

    public void uploadGoal(ActivityGoalModel newGoal) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        activityGoalSync.uploadGoal(newGoal);
    }

    public void syncActivityGoals() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        ActivityGoalDao dao = new ActivityGoalDao(MainActivity.getAppDataBase());
        activityGoalSync.pullGoalsFromCloud(dao);
        activityGoalSync.pushLocalGoalsToCloud(dao);

    }

    public void uploadGeneralGoal(GeneralGoalModel newGoal) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        generalGoalSync.uploadGoal(newGoal);
    }

    public void syncGeneralGoals() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        GeneralGoalDao dao = new GeneralGoalDao(MainActivity.getAppDataBase());
        generalGoalSync.pullGoalsFromCloud(dao);
        generalGoalSync.pushLocalGoalsToCloud(dao);
    }

    public void uploadFoodGoal(FoodGainGoalModel newGoal) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        foodGoalSync.uploadGoal(newGoal);
    }

    public void syncFoodGoals() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        FoodGainGoalDao dao = new FoodGainGoalDao(MainActivity.getAppDataBase());
        foodGoalSync.pullGoalsFromCloud(dao);
        foodGoalSync.pushLocalGoalsToCloud(dao);
    }

    public void uploadDailyActivity(DailyActivityTrackingModel model) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        dailyActivitySync.uploadEntry(model);
    }

    public void syncDailyActivity() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        DailyActivityTrackingDao dao = new DailyActivityTrackingDao(MainActivity.getAppDataBase());
        dailyActivitySync.pullFromCloud(dao);
        dailyActivitySync.pushLocalToCloud(dao);
    }

    public void uploadDailyFood(DailyFoodTrackingModel model) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        dailyFoodSync.uploadEntry(model);
    }

    public void syncDailyFood() {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        DailyFoodTrackingDao dao = new DailyFoodTrackingDao(MainActivity.getAppDataBase());
        dailyFoodSync.pullFromCloud(dao);
        dailyFoodSync.pushLocalToCloud(dao);
    }

    public void syncSingleExercise(ExerciseModel exercise) {
        if (exercise == null || exercise.getExercise_uid() == null) return;

        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
        String exerciseUid = exercise.getExercise_uid();

        // 1. Всегда записываем в очередь изменений перед попыткой отправки
        changeDao.enqueue(exerciseUid, "Workout_ex");

        // 2. Если интернета нет — просто выходим.
        // Данные уже в безопасности в SQLite, processPendingChanges() отправит их позже.
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Сеть недоступна. Упражнение " + exerciseUid + " сохранено в очередь.");
            return;
        }

        // 3. Если интернет есть — пробуем отправить
        List<ExerciseModel> list = new ArrayList<>();
        list.add(exercise);

        workoutSessionSync2.syncSpecificExercises(list, new WorkoutSessionSync2.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем из очереди, так как данные на сервере
                changeDao.removeFromQueue(exerciseUid);
                Log.d(TAG, "Упражнение синхронизировано: " + exerciseUid);
            }

            @Override
            public void onFailure(String error) {
                // Оставляем в очереди, сработает при следующей проверке
                Log.e(TAG, "Ошибка синхронизации упражнения: " + error);
            }
        });
    }

    public void syncMultipleExercise(List<ExerciseModel> exercises) {
        if (exercises == null || exercises.isEmpty()) return;

        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());

        // 1. Добавляем КАЖДОЕ упражнение в очередь изменений
        for (ExerciseModel ex : exercises) {
            if (ex.getExercise_uid() != null) {
                changeDao.enqueue(ex.getExercise_uid(), "Workout_ex");
            }
        }

        // 2. Если интернета нет — просто выходим.
        // Мы уже сохранили всё в SQLite, диалог показывать не нужно.
        if (!isNetworkAvailable()) {
            Log.d("DEBUG_SYNC", "Нет сети. " + exercises.size() + " упражнений в очереди.");
            return;
        }

        // 3. Пытаемся отправить пачкой
        workoutSessionSync2.syncSpecificExercises(exercises, new WorkoutSessionSync2.SyncCallback() {
            @Override
            public void onSuccess() {
                // Если вся пачка ушла успешно — чистим очередь для всех этих UID
                for (ExerciseModel ex : exercises) {
                    changeDao.removeFromQueue(ex.getExercise_uid());
                }
                Log.d("DEBUG_SYNC", "Групповая синхронизация завершена успешно.");
            }

            @Override
            public void onFailure(String error) {
                // В случае ошибки ничего не удаляем, очередь разберется позже
                Log.e("DEBUG_SYNC", "Ошибка групповой синхронизации: " + error);
            }
        });
    }

    public void startWorkoutSync(List<ExerciseModel> exercises) {
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        workoutSessionSync2.startWorkoutSync(exercises);
    }



    public void uploadMeal(MealModel meal) {
        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());

        if (!isNetworkAvailable()) {
            // Если интернета нет, просто кладем в очередь.
            // Когда интернет появится, processPendingChanges всё отправит.
            changeDao.enqueue(meal.getMeal_uid(), "meal");
            return;
        }

        // Если интернет есть, отправляем сразу и чистим очередь на всякий случай
        mealSync.uploadMeal(meal, new MealSync.SyncCallback() {
            @Override
            public void onSuccess() { changeDao.removeFromQueue(meal.getMeal_uid()); }
            @Override
            public void onFailure(String error) { changeDao.enqueue(meal.getMeal_uid(), "meal"); }
        });
    }

    public void syncMealPreset(MealModel meal) {
        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
        Log.d("DEBUG_SYNC", "1. syncMealPreset вызвана для: " + meal.getMeal_uid());

        if (!isNetworkAvailable()) {
            Log.d("DEBUG_SYNC", "2. Интернета нет, только очередь");
            changeDao.enqueue(meal.getMeal_uid(), "meal_preset");
            return;
        }

        Log.d("DEBUG_SYNC", "3. Интернет есть, вызываю mealPresetSync.uploadPreset");
        mealPresetSync.uploadPreset(meal, new MealPresetSync.SyncCallback() {
            @Override
            public void onSuccess() {
                Log.d("DEBUG_SYNC", "4. УСПЕХ на сервере!");
                changeDao.removeFromQueue(meal.getMeal_uid());
            }
            @Override
            public void onFailure(String error) {
                Log.e("DEBUG_SYNC", "4. ОШИБКА: " + error);
                changeDao.enqueue(meal.getMeal_uid(), "meal_preset");
            }
        });
    }



    public void receiveMealFromServer(){
        if (!isNetworkAvailable()) {
            showNoInternetDialog();
            return;
        }
        mealSync.loadAllMeals();
    }

    public void processPendingDeletions() {
        if (!isNetworkAvailable() || userId == null) return;

        DeletionQueueDao queueDao = new DeletionQueueDao(MainActivity.getAppDataBase());
        List<DeletionTask> pendingTasks = queueDao.getAllPendingTasks();

        for (DeletionTask task : pendingTasks) {
            final String currentUid = task.uid;
            final String currentType = task.type;
            final String currentDate = task.data;

            if ("Workout_ex_delete".equals(currentType)) {
                // Проверяем, есть ли дата для удаления упражнения
                if (currentDate == null || currentDate.isEmpty()) {
                    Log.e(TAG, "Пропуск удаления: нет даты для упражнения " + currentUid);
                    queueDao.removeFromQueue(currentUid); // Чистим битую задачу
                    continue;
                }

                // Создаем модель-пустышку для твоей функции removeExerciseFromCloud
                ExerciseModel dummyEx = new ExerciseModel();
                dummyEx.setExercise_uid(currentUid);
                dummyEx.setEx_Data(currentDate);

                workoutSessionSync2.removeExerciseFromCloud(dummyEx, new WorkoutSessionSync2.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        queueDao.removeFromQueue(currentUid);
                        Log.d(TAG, "Упражнение удалено из облака (очередь): " + currentUid);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Ошибка удаления упражнения из очереди: " + error);
                        // Оставляем в очереди для повтора
                    }
                });

                continue; // Идем к следующей задаче
            } else if ("base_ex_delete".equalsIgnoreCase(task.type)) {
                baseExerciseSync.deleteBaseExercise(currentUid, new BaseExerciseSync.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        queueDao.removeFromQueue(currentUid);
                        // Также чистим очередь изменений на всякий случай
                        changeDao.removeFromQueue(currentUid);
                    }
                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Не удалось удалить базовое упр: " + error);
                    }
                });
                continue;
            }else if ("workout_preset_delete".equalsIgnoreCase(task.type)) {
                presetWorkoutSync.deletePresetFromCloud(task.uid, new PresetWorkoutSync.SyncCallback() {
                    @Override
                    public void onSuccess() {
                        queueDao.removeFromQueue(task.uid);
                        new ChangeElmDao(MainActivity.getAppDataBase()).removeFromQueue(task.uid);
                    }
                    @Override
                    public void onFailure(String error) {}
                });
            }

            // --- Старая логика для пресетов и еды (удаление документов целиком) ---
            String collectionPath;
            if ("meal_preset".equals(currentType)) {
                collectionPath = "meal_presets";
            } else if ("food".equals(currentType)) {
                collectionPath = "base_food";
            } else {
                collectionPath = "meal_diary";
            }

            db.collection("users").document(userId)
                    .collection(collectionPath).document(currentUid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        queueDao.removeFromQueue(currentUid);
                        Log.d(TAG, "Удалено из облака (" + currentType + "): " + currentUid);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Ошибка удаления документа: " + currentUid, e));
        }
    }

    public void processPendingChanges() {
        if (!isNetworkAvailable() || userId == null) return;

        ChangeElmDao changeDao = new ChangeElmDao(MainActivity.getAppDataBase());
        List<ChangeElmDao.ChangeTask> tasks = changeDao.getAllTasks();

        if (tasks.isEmpty()) return;

        Log.d(TAG, "Найдено изменений для синхронизации: " + tasks.size());

        for (ChangeElmDao.ChangeTask task : tasks) {
            final String uid = task.uid;

            if ("meal".equals(task.type)) {
                // 1. Собираем модель приема пищи из локальной БД
                MealNameDao mealNameDao = new MealNameDao(MainActivity.getAppDataBase());
                ConnectingMealDao connDao = new ConnectingMealDao(MainActivity.getAppDataBase());
                MealFoodDao foodDao = new MealFoodDao(MainActivity.getAppDataBase());

                MealModel meal = mealNameDao.getMealByUid(uid, connDao, foodDao);

                if (meal != null) {
                    // Вызываем твою функцию (я добавил callback, чтобы удалить из очереди при успехе)
                    mealSync.uploadMeal(meal, new MealSync.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            changeDao.removeFromQueue(uid);
                            Log.d(TAG, "Прием пищи синхронизирован: " + uid);
                        }
                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "Ошибка синхронизации приема пищи: " + error);
                        }
                    });
                } else {
                    // Если модели нет в БД (удалена), просто чистим очередь изменений
                    changeDao.removeFromQueue(uid);
                }

            } else if ("meal_preset".equals(task.type)) {
                // 2. Собираем модель пресета
                PresetMealNameDao presetDao = new PresetMealNameDao(MainActivity.getAppDataBase());
                ConnectingMealPresetDao connPresetDao = new ConnectingMealPresetDao(MainActivity.getAppDataBase());
                PresetEatDao presetEatDao = new PresetEatDao(MainActivity.getAppDataBase());

                MealModel preset = presetDao.getPresetMealByUid(uid, connPresetDao, presetEatDao);

                if (preset != null) {
                    mealPresetSync.uploadPreset(preset, new MealPresetSync.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            changeDao.removeFromQueue(uid);
                            Log.d(TAG, "Пресет синхронизирован: " + uid);
                        }
                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "Ошибка синхронизации пресета: " + error);
                        }
                    });
                } else {
                    changeDao.removeFromQueue(uid);
                }
            }   else if ("Workout_ex".equals(task.type)) {
                // 3. Работаем с упражнениями тренировки
                // Используем твой WORKOUT_EXERCISE_TABLE_DAO
                WORKOUT_EXERCISE_TABLE_DAO workoutExDao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
                // Получаем полную модель упражнения (вместе с сетами) по UID из очереди
                ExerciseModel exercise = workoutExDao.getExByUid(uid);

                if (exercise != null) {
                    List<ExerciseModel> list = new ArrayList<>();
                    list.add(exercise);

                    // Отправляем в облако через существующий метод
                    workoutSessionSync2.syncSpecificExercises(list, new WorkoutSessionSync2.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            // Если в Firebase сохранилось — удаляем из локальной очереди
                            changeDao.removeFromQueue(uid);
                            Log.d(TAG, "Упражнение успешно синхронизировано в облако: " + uid);
                        }

                        @Override
                        public void onFailure(String error) {
                            // В случае сетевой ошибки оставляем в очереди до следующего раза
                            Log.e(TAG, "Ошибка синхронизации упражнения " + uid + ": " + error);
                        }
                    });
                } else {
                    // Если упражнения нет в базе (например, оно было удалено пользователем совсем),
                    // просто убираем битую ссылку из очереди.
                    changeDao.removeFromQueue(uid);
                }
            } else if ("base_exercise".equalsIgnoreCase(task.type)) {
                // 4. НОВОЕ: Базовые упражнения (Библиотека / Custom Exercises)
                BASE_EXERCISE_TABLE_DAO baseDao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
                BaseExModel baseEx = baseDao.getExByUid(uid); // Убедись, что такой метод есть в твоем DAO

                if (baseEx != null) {
                    // Вызываем метод изменения. oldName передаем как текущее имя,
                    // так как в Firestore ID — это UID, и переименование пройдет корректно.
                    baseExerciseSync.syncBaseExerciseChange(baseEx.getBase_ex_name(), baseEx, new BaseExerciseSync.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            changeDao.removeFromQueue(uid);
                            Log.d(TAG, "Базовое упражнение синхронизировано: " + uid);
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "Ошибка синхронизации базового упр: " + error);
                        }
                    });
                } else {
                    // Если упражнение удалено из локальной библиотеки, убираем из очереди изменений
                    changeDao.removeFromQueue(uid);
                }
            }else if ("workout_preset".equalsIgnoreCase(task.type)) {
                WORKOUT_PRESET_NAME_TABLE_DAO presetDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());
                // Используем наш новый метод
                ExerciseModel preset = presetDao.getPresetByUid(uid);

                if (preset != null) {
                    // Вызываем upload с callback
                    presetWorkoutSync.uploadPreset(preset.getExerciseName(), uid, preset.getSets(), new PresetWorkoutSync.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            changeDao.removeFromQueue(uid);
                            Log.d(TAG, "Пресет синхронизирован из очереди: " + uid);
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "Ошибка фоновой синхронизации пресета: " + error);
                        }
                    });
                } else {
                    // Если пресета нет в локальной БД (был удален совсем), чистим очередь изменений
                    changeDao.removeFromQueue(uid);
                }
            }
        }
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager =
                (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        // Используем Handler, чтобы гарантированно запустить в главном потоке
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            // Так как у нас нет ссылки на Activity, используем системный Toast
            // или создаем прозрачную Activity-диалог.
            // Самый простой вариант - Toast, но вы просили Диалог.
            // Чтобы показать именно AlertDialog, нужен Context именно от Activity.

            android.widget.Toast.makeText(context,
                    "Нет подключения к интернету. Проверьте соединение и перезапустите приложение для синхронизации.",
                    android.widget.Toast.LENGTH_LONG).show();

            Log.e(TAG, "Sync failed: No internet connection");
        });
    }
}