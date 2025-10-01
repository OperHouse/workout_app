package com.example.workoutapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Fragments.NutritionFragments.NutritionFragment;
import com.example.workoutapp.Fragments.ProfileFragments.ProfileFragment;
import com.example.workoutapp.Fragments.WorkoutFragments.WorkoutFragment;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final Map<String, Fragment> fragmentCache = new HashMap<>();
    public ActivityMainBinding bindingMain;
    private static AppDataBase appDataBase;

    // --- Кэшированный список упражнений ---
    private List<ExerciseModel> cachedExercises;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        bindingMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingMain.getRoot());

        // Инициализация базы данных
        appDataBase = AppDataBase.getInstance(getApplicationContext());

        bindingMain.bottomNavView.setBackground(null);

        // --- Загружаем упражнения один раз ---
        loadExercisesFromDb();

        //replaceFragment("workout", new WorkoutFragment());
        setInitialActiveButton();

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

        // Скрываем все текущие фрагменты
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


    // --- Метод для загрузки упражнений из БД (вызывается один раз при старте) ---
    public void loadExercisesFromDb() {
        WORKOUT_EXERCISE_TABLE_DAO dao = new WORKOUT_EXERCISE_TABLE_DAO(appDataBase);
        cachedExercises = dao.getExByState("unfinished");
        // Если надо — здесь можно сразу подгрузить подходы для каждого ExerciseModel
        // for (ExerciseModel ex : cachedExercises) { ex.setSets(...); }
    }

    // --- Геттер для доступа из фрагментов ---
    public List<ExerciseModel> getCachedExercises() {
        return cachedExercises;
    }

    // --- Можно добавить метод для обновления кэша, если нужно перезагрузить из БД ---
    public void reloadExercisesFromDb() {
        loadExercisesFromDb();
    }



    public static AppDataBase getAppDataBase() {
        return appDataBase;
    }

    private void setInitialActiveButton() {
        bindingMain.bottomNavView.getMenu().getItem(2).setChecked(true);

        WorkoutFragment fragment = new WorkoutFragment();
        fragment.setExercises(getCachedExercises());
        showOrAddFragment("workout", fragment);
    }
}