package com.example.workoutapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Fragments.NutritionFragments.NutritionFragment;
import com.example.workoutapp.Fragments.ProfileFragments.ProfileFragment;
import com.example.workoutapp.Fragments.WorkoutFragments.WorkoutFragment;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Tools.EncryptionTools.DatabaseProvider;
import com.example.workoutapp.Tools.HealthSettingsActivityTools.HealthConnectHelper;
import com.example.workoutapp.Tools.HealthSettingsActivityTools.HealthConnectReader;
import com.example.workoutapp.Tools.HealthSettingsActivityTools.HealthPermissions;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.example.workoutapp.Tools.SyncTools.FirestoreSyncManager;
import com.example.workoutapp.Tools.SyncTools.SyncWorker;
import com.example.workoutapp.Tools.UidGenerator;
import com.example.workoutapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

        // Инициализация БД и Менеджера синхронизации
        appDataBase = DatabaseProvider.get(this);
        syncManager = new FirestoreSyncManager(this);

        loadExercisesFromDb();
        setInitialActiveButton();

        // Слушатель авторизации для запуска синхронизации при входе
        authStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null && !isInitialSyncDone) {
                Log.d("CloudSync", "Auth подтвержден: " + firebaseAuth.getCurrentUser().getUid());
                startInitialCloudSync();
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

        // Настройка Health Connect
        setupHealthConnect();

        // Bottom navigation
        bindingMain.bottomNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.Profile) showOrAddFragment("profile", new ProfileFragment());
            else if (id == R.id.Workout) {
                WorkoutFragment workoutFragment = new WorkoutFragment();
                workoutFragment.setExercises(getCachedExercises());
                showOrAddFragment("workout", workoutFragment);
            } else if (id == R.id.Nutrition) showOrAddFragment("nutrition", new NutritionFragment());
            else if (id == R.id.People) showOrAddFragment("people", new PeopleFragment());
            return true;
        });
    }

    /**
     * ГЛАВНЫЙ МЕТОД: Начальная синхронизация с жестким ожиданием сервера (15 сек)
     */
    private void startInitialCloudSync() {
        isInitialSyncDone = true;

        // Показываем индикатор загрузки
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Синхронизация с облаком...");
        progress.setCancelable(false);
        progress.show();

        new Thread(() -> {
            try {
                // Небольшая пауза для готовности ресурсов
                Thread.sleep(300);

                if (appDataBase != null && appDataBase.isOpen()) {
                    WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(appDataBase);
                    List<ExerciseModel> allLocalExercises = dao.getAllExercisesForSync();

                    // Защелка для ожидания завершения всей цепочки
                    CountDownLatch latch = new CountDownLatch(1);
                    final boolean[] isServerDone = {false};

                    // Запускаем единую цепочку (Изменения -> Удаления -> Загрузка данных)
                    syncManager.startFullSynchronization(allLocalExercises, () -> {
                        isServerDone[0] = true;
                        latch.countDown();
                    });

                    // Ждем 15 секунд подтверждения от сокета сервера
                    boolean completedOnTime = latch.await(15, TimeUnit.SECONDS);

                    runOnUiThread(() -> {
                        progress.dismiss();
                        if (completedOnTime && isServerDone[0]) {
                            Toast.makeText(this, "Данные синхронизированы", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Сервер не ответил. Синхронизация продолжится в фоне.", Toast.LENGTH_LONG).show();
                            Log.e("CloudSync", "Таймаут синхронизации 15с вышел");
                        }
                    });

                } else {
                    isInitialSyncDone = false;
                    runOnUiThread(progress::dismiss);
                }
            } catch (Exception e) {
                Log.e("CloudSync", "Ошибка синхронизации: " + e.getMessage());
                isInitialSyncDone = false;
                runOnUiThread(progress::dismiss);
            }
        }).start();
    }

    /**
     * Метод для принудительной синхронизации (например, по кнопке в настройках)
     */
    public void forceSyncWithTimeout() {
        startInitialCloudSync(); // Переиспользуем логику с таймаутом
    }

    // ---------- Работа с Фрагментами ----------

    public void showOrAddFragment(String tag, Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f != null) transaction.hide(f);
        }
        Fragment existing = getSupportFragmentManager().findFragmentByTag(tag);
        if (existing == null) {
            transaction.add(R.id.frameLayout, fragment, tag);
            fragmentCache.put(tag, fragment);
            transaction.show(fragment);
        } else {
            transaction.show(existing);
        }
        transaction.commit();
    }

    // ---------- Здоровье (Health Connect) ----------

    private void setupHealthConnect() {
        healthPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean granted : result.values()) { if (!granted) { allGranted = false; break; } }
                    if (allGranted) syncHealthData();
                }
        );
        checkHealthPermissions();
    }

    private void checkHealthPermissions() {
        HealthConnectHelper.checkGrantedPermissions(this, HealthPermissions.INSTANCE.getREQUIRED_PERMISSIONS(),
                grantedPermissions -> {
                    if (!grantedPermissions.containsAll(HealthPermissions.INSTANCE.getREQUIRED_PERMISSIONS())) {
                        healthPermissionLauncher.launch(HealthPermissions.INSTANCE.getREQUIRED_PERMISSIONS().toArray(new String[0]));
                    } else {
                        syncHealthData();
                    }
                    return Unit.INSTANCE;
                });
    }

    private void syncHealthData() {
        HealthConnectReader reader = new HealthConnectReader(this);
        reader.readToday(data -> {
            if (data.getSteps() > 0 || data.getCalories() > 0) {
                String todayDate = java.time.LocalDate.now().toString();
                DailyActivityTrackingDao dao = new DailyActivityTrackingDao(appDataBase);
                DailyActivityTrackingModel existing = dao.getActivityByDate(todayDate);

                String uid = (existing != null) ? existing.getDaily_activity_tracking_uid() : UidGenerator.generateDailyActivityUid();
                DailyActivityTrackingModel model = new DailyActivityTrackingModel(0, todayDate, data.getSteps(), (float) data.getCalories(), uid, (float) data.getDistance());

                dao.insertOrUpdate(model);
                if (syncManager != null) syncManager.uploadDailyActivity(model);
            }
            return Unit.INSTANCE;
        });
    }

    // ---------- Утилиты БД ----------

    public void loadExercisesFromDb() {
        WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(appDataBase);
        cachedExercises = dao.getExByState("unfinished");
    }

    public List<ExerciseModel> getCachedExercises() { return cachedExercises; }

    public static SQLiteDatabase getAppDataBase() { return appDataBase; }

    public static FirestoreSyncManager getSyncManager() { return syncManager; }

    private void setInitialActiveButton() {
        bindingMain.bottomNavView.getMenu().getItem(2).setChecked(true);
        WorkoutFragment fragment = new WorkoutFragment();
        fragment.setExercises(getCachedExercises());
        showOrAddFragment("workout", fragment);
    }

    @Override
    public void setBottomNavVisibility(boolean isVisible) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }
    public void reloadExercisesFromDb() {
        loadExercisesFromDb();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authStateListener != null) FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    public static void schedulePeriodicBackupSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // Минимальный интервал в Android - 15 минут.
        // Этого достаточно, чтобы "подчистить" хвосты, если приложение закрыто.
        PeriodicWorkRequest periodicSyncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag("periodic_backup_sync")
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "unique_periodic_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
        );
    }
}