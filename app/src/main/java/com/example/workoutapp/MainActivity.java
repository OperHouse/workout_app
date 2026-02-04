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

import com.example.workoutapp.Data.EncryptionTools.DatabaseProvider;
import com.example.workoutapp.Data.ProfileDao.DailyActivityTrackingDao;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Fragments.NutritionFragments.NutritionFragment;
import com.example.workoutapp.Fragments.ProfileFragments.ProfileFragment;
import com.example.workoutapp.Fragments.WorkoutFragments.WorkoutFragment;
import com.example.workoutapp.Models.ProfileModels.DailyActivityTrackingModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.example.workoutapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

    private ActivityResultLauncher<String[]> healthPermissionLauncher;

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


        loadExercisesFromDb();
        setInitialActiveButton();

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
            // Теперь DailyHealthData содержит и getSteps(), и getCalories()
            DailyActivityTrackingModel model = new DailyActivityTrackingModel(
                    0,
                    java.time.LocalDate.now().toString(),
                    data.getSteps(),
                    data.getCalories() // ТЕПЕРЬ СОХРАНЯЕМ КАЛОРИИ
            );

            DailyActivityTrackingDao dao = new DailyActivityTrackingDao(appDataBase);
            dao.insertOrUpdate(model);

            Log.d("HealthConnect", "Data saved. Steps: " + data.getSteps() + ", Cals: " + data.getCalories());
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
}
