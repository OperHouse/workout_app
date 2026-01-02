package com.example.workoutapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.health.connect.client.HealthConnectClient;
import androidx.health.connect.client.permission.HealthPermission;

import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Fragments.NutritionFragments.NutritionFragment;
import com.example.workoutapp.Fragments.ProfileFragments.ProfileFragment;
import com.example.workoutapp.Fragments.WorkoutFragments.WorkoutFragment;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.example.workoutapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord;
import androidx.health.connect.client.request.ReadRecordsRequest;
import androidx.health.connect.client.response.ReadRecordsResponse; // ВАЖНО: исправлен импорт
import androidx.health.connect.client.time.TimeRangeFilter;
import com.google.common.util.concurrent.ListenableFuture; // ВАЖНО

import java.time.Instant;
import java.util.Calendar; // Для совместимости с API 24
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import kotlin.jvm.JvmClassMappingKt;

public class MainActivity extends AppCompatActivity implements OnNavigationVisibilityListener {
    private static final Map<String, Fragment> fragmentCache = new HashMap<>();
    public ActivityMainBinding bindingMain;
    private static AppDataBase appDataBase;
    private BottomNavigationView bottomNavigationView;
    private List<ExerciseModel> cachedExercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        bindingMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingMain.getRoot());
        bottomNavigationView = findViewById(R.id.bottomNavView);

        appDataBase = AppDataBase.getInstance(getApplicationContext());
        bindingMain.bottomNavView.setBackground(null);

        loadExercisesFromDb();
        setInitialActiveButton();

        // Запуск синхронизации
        //startHealthSync();

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

    public void showOrAddFragment(String tag, Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (Fragment f : getSupportFragmentManager().getFragments()) { if (f != null) transaction.hide(f); }
        Fragment existing = getSupportFragmentManager().findFragmentByTag(tag);
        if (existing == null) {
            transaction.add(R.id.frameLayout, fragment, tag);
            fragmentCache.put(tag, fragment);
            transaction.show(fragment);
        } else { transaction.show(existing); }
        transaction.commit();
    }

    public void loadExercisesFromDb() {
        WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(appDataBase);
        cachedExercises = dao.getExByState("unfinished");
    }

    public List<ExerciseModel> getCachedExercises() { return cachedExercises; }
    public void reloadExercisesFromDb() { loadExercisesFromDb(); }
    public static AppDataBase getAppDataBase() { return appDataBase; }

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

//    // Блок Health Connect (Фитнес браслет)
//    public void startHealthSync() {
//        // Проверка доступности SDK
//        if (HealthConnectClient.isAvailable(this)) {
//            fetchHealthData();
//        } else {
//            // Выводим уведомление или логируем ошибку
//            Log.e("HealthConnect", "SDK недоступен");
//        }
//    }
//
//    public void fetchHealthData() {
//        HealthConnectClient client = HealthConnectClient.getOrCreate(this);
//
//        // API 24 Friendly: Получаем начало дня (00:00)
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MILLISECOND, 0);
//
//        Instant startOfDay = Instant.ofEpochMilli(cal.getTimeInMillis());
//        Instant now = Instant.now();
//        TimeRangeFilter timeRange = TimeRangeFilter.between(startOfDay, now);
//
//        // Асинхронный запрос с Executor
//        Executors.newSingleThreadExecutor().execute(() -> {
//            try {
//                // Запрос для шагов
//                ReadRecordsRequest<StepsRecord> stepsRequest = new ReadRecordsRequest<>(
//                        StepsRecord.class, timeRange, Collections.emptySet(), false, 1000, null
//                );
//
//                List<StepsRecord> stepsRecords = client.readRecords(stepsRequest).getRecords();
//
//                long totalSteps = 0;
//                for (StepsRecord record : stepsRecords) {
//                    totalSteps += record.getCount();
//                }
//
//                // Запрос для калорий
//                ReadRecordsRequest<TotalCaloriesBurnedRecord> caloriesRequest = new ReadRecordsRequest<>(
//                        TotalCaloriesBurnedRecord.class, timeRange, Collections.emptySet(), false, 1000, null
//                );
//
//                List<TotalCaloriesBurnedRecord> caloriesRecords = client.readRecords(caloriesRequest).getRecords();
//                double totalCalories = 0;
//                for (TotalCaloriesBurnedRecord record : caloriesRecords) {
//                    totalCalories += record.getEnergy().getKilocalories();
//                }
//
//                saveHealthDataToDb(totalSteps, (float) totalCalories);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//
//    private void saveHealthDataToDb(long steps, float calories) {
//        DailyActivityTrackingDao dao = new DailyActivityTrackingDao(appDataBase);
//        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
//                .format(new java.util.Date());
//
//        DailyActivityTrackingModel model = new DailyActivityTrackingModel(
//                0, todayDate, (int) steps, calories
//        );
//        dao.insertOrUpdate(model);
//    }
}
