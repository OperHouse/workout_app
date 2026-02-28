package com.example.workoutapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Fragments.NutritionFragments.NutritionFragment;
import com.example.workoutapp.Fragments.ProfileFragments.ProfileFragment;
import com.example.workoutapp.Fragments.WorkoutFragments.WorkoutFragment;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Tools.EncryptionTools.DatabaseProvider;
import com.example.workoutapp.Tools.FirestoreSyncManager;
import com.example.workoutapp.Tools.HealthSettingsActivityTools.HealthConnectHelper;
import com.example.workoutapp.Tools.HealthSettingsActivityTools.HealthConnectReader;
import com.example.workoutapp.Tools.HealthSettingsActivityTools.HealthPermissions;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.example.workoutapp.Tools.UidGenerator;
import com.example.workoutapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;

public class MainActivity extends AppCompatActivity
        implements OnNavigationVisibilityListener {

    private static final Map<String, Fragment> fragmentCache = new HashMap<>();
    private ActivityMainBinding bindingMain;
    private static SQLiteDatabase appDataBase;
    private BottomNavigationView bottomNavigationView;
    private List<ExerciseModel> cachedExercises;

    private FirebaseAuth.AuthStateListener authStateListener;

    private ActivityResultLauncher<String[]> healthPermissionLauncher;
    private static FirestoreSyncManager syncManager;
    private boolean isInitialSyncDone = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        bindingMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingMain.getRoot());

        bottomNavigationView = findViewById(R.id.bottomNavView);
        bindingMain.bottomNavView.setBackground(null);

        appDataBase = DatabaseProvider.get(this);
        //DatabaseExporter.exportDatabase(this, "WorkoutApp_plain.db");
        syncManager = new FirestoreSyncManager();

        loadExercisesFromDb();
        setInitialActiveButton();

        // 2. Вместо простого вызова метода, вешаем слушатель
        authStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null && !isInitialSyncDone) {
                Log.d("CloudSync", "Auth подтвержден: " + firebaseAuth.getCurrentUser().getUid());
                startInitialCloudSync();
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);



        // ---------- Health Connect permission launcher ----------
        healthPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {
                            // Проверяем, выданы ли ВСЕ запрошенные разрешения (шаги и калории)
                            boolean allGranted = true;
                            for (Boolean granted : result.values()) {
                                if (!granted) {
                                    allGranted = false;
                                    break;
                                }
                            }

                            if (allGranted) {
                                syncHealthData();
                            } else {
                                Log.e("HealthConnect", "Не все разрешения выданы пользователем");
                            }
                        }
                );

        checkHealthPermissions();

        // ---------- Bottom navigation ----------
        bindingMain.bottomNavView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.Profile) {
                showOrAddFragment("profile", new ProfileFragment());
            } else if (item.getItemId() == R.id.Workout) {
                WorkoutFragment workoutFragment = new WorkoutFragment();
                workoutFragment.setExercises(getCachedExercises());
                showOrAddFragment("workout", workoutFragment);
            } else if (item.getItemId() == R.id.Nutrition) {
                showOrAddFragment("nutrition", new NutritionFragment());
            } else if (item.getItemId() == R.id.People) {
                showOrAddFragment("people", new PeopleFragment());
            }
            return true;
        });
    }

    // ---------- Health Connect permissions ----------

    private void checkHealthPermissions() {
        // Проверяем текущее состояние разрешений через наш Helper
        HealthConnectHelper.checkGrantedPermissions(
                this,
                HealthPermissions.INSTANCE.getREQUIRED_PERMISSIONS(),
                grantedPermissions -> {
                    Log.d("HealthConnect", "Granted permissions: " + grantedPermissions);

                    // Если количество выданных разрешений меньше, чем нам требуется
                    if (!grantedPermissions.containsAll(
                            HealthPermissions.INSTANCE.getREQUIRED_PERMISSIONS())) {

                        String[] permissionsArray =
                                HealthPermissions.INSTANCE.getREQUIRED_PERMISSIONS().toArray(new String[0]);
                        healthPermissionLauncher.launch(permissionsArray);
                    } else {
                        // Все разрешения (шаги + калории) есть, запускаем синхронизацию
                        syncHealthData();
                    }
                    return Unit.INSTANCE;
                }
        );
    }

    /** Синхронизация шагов с SQLite */
    private void syncHealthData() {
        HealthConnectReader reader = new HealthConnectReader(this);

        reader.readToday(data -> {
            // Теперь читаем и дистанцию тоже (data.getDistance())
            if (data.getSteps() > 0 || data.getCalories() > 0 || data.getDistance() > 0) {

                String todayDate = java.time.LocalDate.now().toString();
                DailyActivityTrackingDao dao = new DailyActivityTrackingDao(appDataBase);

                // 1. Проверяем, есть ли запись за сегодня локально
                DailyActivityTrackingModel existing = dao.getActivityByDate(todayDate);

                String uid;
                if (existing != null) {
                    uid = existing.getDaily_activity_tracking_uid();
                } else {
                    // Если локально нет, используем дату как основу для UID или просто создаем новый
                    // Т.к. в облаке ID документа = Дата, конфликта не будет
                    uid = UidGenerator.generateDailyActivityUid();
                }

                DailyActivityTrackingModel model = new DailyActivityTrackingModel(
                        0,
                        todayDate,
                        data.getSteps(),
                        (float) data.getCalories(),
                        uid,
                        (float) data.getDistance()
                );

                // 2. Сохраняем/обновляем локально
                dao.insertOrUpdate(model);

                // 3. Отправляем в облако.
                // Если в облаке уже есть запись с этим ID (датой), она просто обновится новыми значениями
                if (syncManager != null) {
                    syncManager.uploadDailyActivity(model);
                }

                Log.d("HealthConnect", "Saved: Steps=" + data.getSteps() + ", Dist=" + data.getDistance());
            }
            return Unit.INSTANCE;
        });
    }



    // ---------- Fragment handling ----------
    public void showOrAddFragment(String tag, Fragment fragment) {
        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();

        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f != null) transaction.hide(f);
        }

        Fragment existing =
                getSupportFragmentManager().findFragmentByTag(tag);

        if (existing == null) {
            transaction.add(R.id.frameLayout, fragment, tag);
            fragmentCache.put(tag, fragment);
            transaction.show(fragment);
        } else {
            transaction.show(existing);
        }

        transaction.commit();
    }

    // ---------- Database ----------
    public void loadExercisesFromDb() {
        WORKOUT_EXERCISE_TABLE_DAO dao =
                new WORKOUT_EXERCISE_TABLE_DAO(appDataBase);
        cachedExercises = dao.getExByState("unfinished");
    }

    public void reloadExercisesFromDb(){loadExercisesFromDb();}

    public List<ExerciseModel> getCachedExercises() {
        return cachedExercises;
    }

    public static SQLiteDatabase getAppDataBase() {
        if (appDataBase == null) {
        }
        return appDataBase;
    }

    private void setInitialActiveButton() {
        bindingMain.bottomNavView.getMenu().getItem(2).setChecked(true);
        WorkoutFragment fragment = new WorkoutFragment();
        fragment.setExercises(getCachedExercises());
        showOrAddFragment("workout", fragment);
    }

    // ---------- Bottom nav visibility ----------
    @Override
    public void setBottomNavVisibility(boolean isVisible) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(
                    isVisible ? View.VISIBLE : View.GONE
            );
        }
    }


    private void syncDataWithCloud() {
        // 1. Создаем DAO для доступа к SQLite
        WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(appDataBase);

        // 2. Выгружаем все данные из локальной базы
        List<ExerciseModel> allLocalExercises = dao.getAllExercisesForSync();

        if (allLocalExercises != null && !allLocalExercises.isEmpty()) {
            // 3. Создаем объект менеджера
            FirestoreSyncManager syncManager = new FirestoreSyncManager();

            // 4. ВЫЗЫВАЕМ ТОЛЬКО ЭТОТ МЕТОД
            // Он внутри себя сам всё сгруппирует и вызовет uploadWorkoutSession
            syncManager.syncAllWorkouts(allLocalExercises);

            Log.d("CloudSync", "Запущена синхронизация для " + allLocalExercises.size() + " записей");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
    }

    private void startInitialCloudSync() {
        // Ставим флаг СРАЗУ, чтобы не запустить поток дважды
        isInitialSyncDone = true;

        new Thread(() -> {
            try {
                Log.d("CloudSync", "ВХОД В ПОТОК СИНХРОНИЗАЦИИ");

                // Даем базе время инициализироваться
                Thread.sleep(100);

                if (appDataBase != null && appDataBase.isOpen()) {
                    WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(appDataBase);
                    List<ExerciseModel> allLocalExercises = dao.getAllExercisesForSync();

                    Log.d("CloudSync", "Запуск startFullSynchronization. Локальных данных: " +
                            (allLocalExercises != null ? allLocalExercises.size() : 0));

                    syncManager.startFullSynchronization(allLocalExercises);
                } else {
                    Log.e("CloudSync", "БАЗА НЕ ГОТОВА");
                    isInitialSyncDone = false; // Сбрасываем флаг для повторной попытки
                }
            } catch (Exception e) {
                Log.e("CloudSync", "ОШИБКА В ПОТОКЕ: " + e.getMessage());
                isInitialSyncDone = false;
            }
        }).start();
    }

    public static FirestoreSyncManager getSyncManager() {
        return syncManager;
    }
}
