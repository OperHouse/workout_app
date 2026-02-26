package com.example.workoutapp.Fragments.ProfileFragments.Divide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.Data.ProfileDao.FoodGainGoalDao; // Предполагаемый DAO
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.FoodGainGoalModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FoodGoalFragment extends Fragment {

    private OnNavigationVisibilityListener navigationListener;

    private EditText caloriesGoalEdit, proteinGoalEdit, fatGoalEdit, carbGoalEdit;
    private FoodGainGoalDao foodGainGoalDao;

    public FoodGoalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализация DAO
        foodGainGoalDao = new FoodGainGoalDao(MainActivity.getAppDataBase());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_goal, container, false);

        // Инициализация элементов UI
        ImageButton imageButtonBack = view.findViewById(R.id.imageButtonBack);
        MaterialButton saveBtn = view.findViewById(R.id.buttonSave);

        caloriesGoalEdit = view.findViewById(R.id.editTextCalorieGoal);
        proteinGoalEdit = view.findViewById(R.id.editTextProteinGoal);
        fatGoalEdit = view.findViewById(R.id.editTextFatGoal);
        carbGoalEdit = view.findViewById(R.id.editTextCarbGoal);

        // Кнопка "Назад"
        imageButtonBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // 🔹 Скрытие клавиатуры по клику на пустое место
        ConstraintLayout rootLayout = view.findViewById(R.id.rootConstraintLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false;
        });

        // 🔹 Обработка Enter/Next на клавиатуре
        setupEditorActionListener(caloriesGoalEdit);
        setupEditorActionListener(proteinGoalEdit);
        setupEditorActionListener(fatGoalEdit);
        setupEditorActionListener(carbGoalEdit);

        // Кнопка сохранения
        saveBtn.setOnClickListener(v -> saveFoodGoal());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadFoodGoalData(); // 🔹 Подгружаем данные из БД
    }

    // 🔹 Настройка слушателя для кнопок "Далее"/"Готово"
    private void setupEditorActionListener(EditText editText) {
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                hideKeyboardAndClearFocus();
                return true;
            }
            return false;
        });
    }

    // 🔹 Скрытие клавиатуры и снятие фокуса
    private void hideKeyboardAndClearFocus() {
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            focusedView.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
    }

    // 🔹 Управление видимостью нижней панели навигации
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationVisibilityListener) {
            navigationListener = (OnNavigationVisibilityListener) context;
        } else {
            // Эта ошибка будет выброшена, если MainActivity не реализует интерфейс
            throw new RuntimeException(context + " must implement OnNavigationVisibilityListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationListener != null) {
            navigationListener.setBottomNavVisibility(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (navigationListener != null) {
            navigationListener.setBottomNavVisibility(true);
        }
    }

    // 🔹 Загрузка данных из базы в поля
    private void loadFoodGoalData() {
        FoodGainGoalModel lastGoal = foodGainGoalDao.getLatestGoal();

        if (lastGoal != null) {
            if (lastGoal.getFood_gain_goal_calories() > 0) {
                caloriesGoalEdit.setText(String.valueOf(lastGoal.getFood_gain_goal_calories()));
            }
            if (lastGoal.getFood_gain_goal_protein() > 0) {
                // Используем Locale.getDefault() для корректного форматирования float
                proteinGoalEdit.setText(String.format(Locale.getDefault(), "%.0f", lastGoal.getFood_gain_goal_protein()));
            }
            if (lastGoal.getFood_gain_goal_fat() > 0) {
                fatGoalEdit.setText(String.format(Locale.getDefault(), "%.0f", lastGoal.getFood_gain_goal_fat()));
            }
            if (lastGoal.getFood_gain_goal_carb() > 0) {
                carbGoalEdit.setText(String.format(Locale.getDefault(), "%.0f", lastGoal.getFood_gain_goal_carb()));
            }
        }
    }

    // 🔹 Сохранение данных
    private void saveFoodGoal() {
        int calories = 0;
        float protein = 0f;
        float fat = 0f;
        float carb = 0f;
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(new Date(currentTimeMillis));

        // Парсинг значений. Используем try-catch для безопасной обработки ввода.
        try { calories = Integer.parseInt(caloriesGoalEdit.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        try { protein = Float.parseFloat(proteinGoalEdit.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        try { fat = Float.parseFloat(fatGoalEdit.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        try { carb = Float.parseFloat(carbGoalEdit.getText().toString().trim()); } catch (NumberFormatException ignored) {}

        // Простая валидация: все значения должны быть больше нуля
        if (calories <= 0 || protein <= 0 || fat <= 0 || carb <= 0) {
            Toast.makeText(requireContext(), "Пожалуйста, заполните все поля корректными числами.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Создаем модель
        FoodGainGoalModel newGoal = new FoodGainGoalModel(
                0, // ID будет присвоен автоматически
                calories,
                protein,
                fat,
                carb,
                formattedDate
        );

        // 2. Сохраняем в базу данных
        foodGainGoalDao.insertGoal(newGoal);

        Toast.makeText(requireContext(), "Цели питания успешно сохранены.", Toast.LENGTH_SHORT).show();

        // 3. Возврат на предыдущий экран
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }
}