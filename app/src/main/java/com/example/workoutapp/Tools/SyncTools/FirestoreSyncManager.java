package com.example.workoutapp.Tools.SyncTools;

import android.content.Context;
import android.util.Log;

import com.example.workoutapp.Data.ChangeElmDao;
import com.example.workoutapp.Data.DeletionQueueDao;
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

    public BaseExerciseSync baseExerciseSync;
    public ProfileSync profileSync;
    public PresetWorkoutSync presetWorkoutSync;
    public  WeightSync weightSync;
    public  ActivityGoalSync activityGoalSync;
    public  GeneralGoalSync generalGoalSync;
    public  FoodGoalSync foodGoalSync;
    public  DailyActivitySync dailyActivitySync;
    public  DailyFoodSync dailyFoodSync;
    public  WorkoutSessionSync2 workoutSessionSync2;
    public  BaseFoodSync baseFoodSync;
    public  MealPresetSync mealPresetSync;
    public  MealSync mealSync;

    private ListenerRegistration foodListener;

    private final Context context;
    private ChangeElmDao changeDao;
    DeletionQueueDao deletionQueueDao;


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
        this.deletionQueueDao = new DeletionQueueDao(MainActivity.getAppDataBase());

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
        //profileSync.syncProfile();
        //weightSync.syncWeightHistory();

        //syncActivityGoals();
        //syncGeneralGoals();
        //syncFoodGoals();
        //syncDailyActivity();
        //syncDailyFood();

        //startWorkoutSync(localExercises);

        // ЕДА теперь синкается через realtime listener
        //startFoodRealtimeSync();

        Log.d(TAG, "Starting sync of all meal presets...");
        //mealPresetSync.downloadAllOnce();

        //receiveMealFromServer();


        Log.d(TAG, "Full sync initialized");
    }

    // =====================================================
    // ОСТАЛЬНЫЕ СИНХРОНИЗАЦИИ
    // =====================================================

    public void syncPresetUpdate(String name, String uid, List<ExerciseModel> exercises) {
        if (uid == null) return;

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
        // 2. Ставим в очередь на синхронизацию.
        // Поскольку запись одна, используем фиксированный ключ "singleton_profile"
        final String syncKey = "singleton_profile";
        changeDao.enqueue(syncKey, "user_profile");

        if (!isNetworkAvailable()) {
            Log.d("ProfileSync", "Офлайн. Профиль обновлен локально и ждет сети.");
            return;
        }

        // 3. Если сеть есть — отправляем
        profileSync.uploadProfile(profile, new ProfileSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем из очереди, когда Firestore подтвердил получение
                changeDao.removeFromQueue(syncKey);
                Log.d("ProfileSync", "Профиль успешно улетел в Firestore");
            }

            @Override
            public void onFailure(String error) {
                Log.e("ProfileSync", "Ошибка сети, профиль остался в очереди: " + error);
            }
        });
    }

    public void syncNewWeight(WeightHistoryModel weightEntry) {
        if (weightEntry == null || weightEntry.getWeight_history_uid() == null) return;

        final String uid = weightEntry.getWeight_history_uid();

        // 1. Записываем конкретную запись веса в очередь (тип "weight_history")
        changeDao.enqueue(uid, "weight_history");

        if (!isNetworkAvailable()) {
            Log.d(TAG, "Офлайн. Запись веса (" + uid + ") сохранена в очередь.");
            // Здесь не показываем диалог "Нет интернета", чтобы не мешать пользователю,
            // так как запись уже в очереди и сохранится локально.
            return;
        }

        // 2. Пытаемся отправить в Firebase
        weightSync.uploadWeightEntry(weightEntry, new WeightSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем из очереди только при успехе в облаке
                changeDao.removeFromQueue(uid);
                Log.d(TAG, "Синхронизация веса завершена: " + uid);
            }

            @Override
            public void onFailure(String error) {
                // Оставляем в очереди, фоновый процесс попробует снова
                Log.e(TAG, "Ошибка синхронизации веса: " + error);
            }
        });
    }


    public void uploadActivityGoal(ActivityGoalModel newGoal) {
        if (newGoal == null || newGoal.getActivity_goal_uid() == null) return;

        final String uid = newGoal.getActivity_goal_uid();

        // 1. Записываем в очередь (тип "activity_goal")
        changeDao.enqueue(uid, "activity_goal");

        if (!isNetworkAvailable()) {
            Log.d(TAG, "Офлайн. Цель сохранена в очередь.");
            return;
        }

        // 2. Пытаемся отправить
        activityGoalSync.uploadGoal(newGoal, new ActivityGoalSync.SyncCallback() {
            @Override
            public void onSuccess() {
                changeDao.removeFromQueue(uid);
                Log.d(TAG, "Цель активности синхронизирована успешно.");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка синхронизации цели: " + error);
            }
        });
    }

    public void uploadGeneralGoal(GeneralGoalModel newGoal) {
        if (newGoal == null || newGoal.getGeneral_goal_uid() == null) return;

        final String uid = newGoal.getGeneral_goal_uid();

        // 1. Записываем в очередь (тип "general_goal")
        changeDao.enqueue(uid, "general_goal");

        if (!isNetworkAvailable()) {
            Log.d(TAG, "Офлайн. Общая цель сохранена в очередь.");
            return;
        }

        // 2. Пытаемся отправить немедленно
        generalGoalSync.uploadGoal(newGoal, new GeneralGoalSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем из очереди только при подтверждении сервером
                changeDao.removeFromQueue(uid);
                Log.d(TAG, "Общая цель успешно синхронизирована.");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка синхронизации цели: " + error);
            }
        });
    }

    public void uploadFoodGoal(FoodGainGoalModel newGoal) {
        if (newGoal == null || newGoal.getFood_gain_goal_uid() == null) return;

        final String uid = newGoal.getFood_gain_goal_uid();

        // 1. Записываем в очередь (тип "food_goal")
        changeDao.enqueue(uid, "food_goal");

        if (!isNetworkAvailable()) {
            Log.d(TAG, "Офлайн. Цель питания сохранена в очередь.");
            return;
        }

        // 2. Пытаемся отправить немедленно
        foodGoalSync.uploadGoal(newGoal, new FoodGoalSync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем из очереди только после подтверждения сервером
                changeDao.removeFromQueue(uid);
                Log.d(TAG, "Цель питания успешно синхронизирована.");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка синхронизации цели: " + error);
            }
        });
    }

    public void uploadDailyActivity(DailyActivityTrackingModel model) {
        if (model == null || model.getDaily_activity_tracking_date() == null) return;

        final String dateKey = model.getDaily_activity_tracking_date();

        // 1. Ставим в очередь (тип "daily_activity")
        changeDao.enqueue(dateKey, "daily_activity");

        if (!isNetworkAvailable()) {
            Log.d(TAG, "Офлайн. Активность за " + dateKey + " сохранена в очередь.");
            return;
        }

        // 2. Попытка немедленной отправки
        dailyActivitySync.uploadEntry(model, new DailyActivitySync.SyncCallback() {
            @Override
            public void onSuccess() {
                // Удаляем только после подтверждения сервером
                changeDao.removeFromQueue(dateKey);
                Log.d(TAG, "Активность синхронизирована успешно.");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка немедленной синхронизации: " + error);
            }
        });
    }


    public void uploadDailyFood(DailyFoodTrackingModel model) {
        if (model == null || model.getDaily_food_tracking_date() == null) return;

        final String dateKey = model.getDaily_food_tracking_date();

        // 1. Ставим в очередь (тип "daily_food")
        changeDao.enqueue(dateKey, "daily_food");

        if (!isNetworkAvailable()) {
            Log.d(TAG, "Офлайн. Данные КБЖУ сохранены в очередь.");
            return;
        }

        // 2. Попытка немедленной отправки
        dailyFoodSync.uploadEntry(model, new DailyFoodSync.SyncCallback() {
            @Override
            public void onSuccess() {
                changeDao.removeFromQueue(dateKey);
                Log.d(TAG, "КБЖУ за " + dateKey + " успешно синхронизированы.");
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Ошибка немедленной синхронизации КБЖУ: " + error);
            }
        });
    }

    public void syncSingleExercise(ExerciseModel exercise) {
        if (exercise == null || exercise.getExercise_uid() == null) return;

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

        List<DeletionTask> pendingTasks = deletionQueueDao.getAllPendingTasks();

        if (pendingTasks.isEmpty()) return;

        Log.d(TAG, "Найдено задач на удаление: " + pendingTasks.size());

        // Создаем процессор
        DeletionTaskProcessor processor = new DeletionTaskProcessor(this);

        for (DeletionTask task : pendingTasks) {
            processor.process(task);
        }
    }

    public void processPendingChanges() {
        // Проверка сети и авторизации
        if (!isNetworkAvailable() || userId == null) return;

        List<ChangeElmDao.ChangeTask> tasks = changeDao.getAllTasks();

        if (tasks.isEmpty()) return;

        Log.d(TAG, "Найдено изменений для синхронизации: " + tasks.size());

        // Создаем экземпляр нашего нового процессора
        ChangeUploader processor = new ChangeUploader(this);

        for (ChangeElmDao.ChangeTask task : tasks) {
            // Вызываем логику через switch-case внутри процессора
            processor.process(task);
        }
    }



    public void downloadAllDataFromCloud() {
        if (!isNetworkAvailable() || userId == null) return;

        InitialDataDownloader downloader = new InitialDataDownloader(this);
        downloader.downloadEverything();
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