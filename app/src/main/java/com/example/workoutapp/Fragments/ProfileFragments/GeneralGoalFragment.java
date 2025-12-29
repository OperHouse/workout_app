package com.example.workoutapp.Fragments.ProfileFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.Data.ProfileDao.GeneralGoalDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.GeneralGoalModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GeneralGoalFragment extends Fragment {

    private OnNavigationVisibilityListener navigationListener;
    private AutoCompleteTextView generalGoalView, workoutGoalView, nutritionGoalView;
    private MaterialButton saveBtn;

    // Массивы для выпадающих списков
    private final String[] goals = {"Похудение", "Поддержание", "Набор", "Рекомпозиция", "Марафон", "Силовые тренировки", "Гибкость"};
    private final String[] weeklyCounts = {"1", "2", "3", "4", "5", "6", "7"};


    public GeneralGoalFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_general_goal, container, false);

        // Инициализация элементов
        ImageButton imageButtonBack = view.findViewById(R.id.imageButtonBack);
        saveBtn = view.findViewById(R.id.buttonSave);
        generalGoalView = view.findViewById(R.id.autoCompleteTextViewGeneralGoal);
        workoutGoalView = view.findViewById(R.id.autoCompleteTextViewWorkoutGeneralGoal);
        nutritionGoalView = view.findViewById(R.id.autoCompleteTextViewNutritionGeneralGoal);

        // Кнопка "Назад"
        imageButtonBack.setOnClickListener(view1 -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // 1. Настройка AutoCompleteTextView для Общей Цели
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinners_style, goals);
        generalGoalView.setAdapter(goalAdapter);
        // Показ выпадающего списка при нажатии на поле
        generalGoalView.setOnClickListener(v -> generalGoalView.showDropDown());

        // 2. Настройка AutoCompleteTextView для Тренировок в неделю
        ArrayAdapter<String> workoutAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinners_style, weeklyCounts);
        workoutGoalView.setAdapter(workoutAdapter);
        workoutGoalView.setOnClickListener(v -> workoutGoalView.showDropDown());

        // 3. Настройка AutoCompleteTextView для Записи питания в неделю
        ArrayAdapter<String> nutritionAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinners_style, weeklyCounts);
        nutritionGoalView.setAdapter(nutritionAdapter);
        nutritionGoalView.setOnClickListener(v -> nutritionGoalView.showDropDown());

        // Скрытие клавиатуры по клику на пустое место (Функционал фокуса)
        ConstraintLayout rootLayout = view.findViewById(R.id.rootConstraintLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            return false;
        });

        // Кнопка сохранения
        saveBtn.setOnClickListener(v -> saveGeneralGoal());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadGeneralGoalData(); // Подгружаем данные из БД
    }

    // 🔹 Управление видимостью нижней панели навигации
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavigationVisibilityListener) {
            navigationListener = (OnNavigationVisibilityListener) context;
        } else {
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
            // Скрываем нижнюю панель, когда фрагмент активен
            navigationListener.setBottomNavVisibility(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (navigationListener != null) {
            // Показываем нижнюю панель, когда фрагмент покидает активное состояние
            navigationListener.setBottomNavVisibility(true);
        }
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

    // 🔹 Загрузка данных из базы в поля
    private void loadGeneralGoalData() {
        GeneralGoalDao goalDao = new GeneralGoalDao(MainActivity.getAppDataBase());
        GeneralGoalModel lastGoal = goalDao.getLatestGoal();

        if (lastGoal != null) {
            if (lastGoal.getGoalText() != null && !lastGoal.getGoalText().isEmpty()) {
                // false → чтобы не триггерить адаптер/фильтр
                generalGoalView.setText(lastGoal.getGoalText(), false);
            }
            if (lastGoal.getWorkoutsWeekly() > 0) {
                workoutGoalView.setText(String.valueOf(lastGoal.getWorkoutsWeekly()), false);
            }
            if (lastGoal.getFoodTrackingWeekly() > 0) {
                nutritionGoalView.setText(String.valueOf(lastGoal.getFoodTrackingWeekly()), false);
            }
        }
    }

    // 🔹 Сохранение данных
    private void saveGeneralGoal() {
        String selectedGoal = generalGoalView.getText().toString().trim();
        int workoutsWeekly = 0;
        int foodTrackingWeekly = 0;
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(new Date(currentTimeMillis));

        // Конвертация строковых значений из AutoCompleteTextView в int
        try {
            workoutsWeekly = Integer.parseInt(workoutGoalView.getText().toString().trim());
        } catch (NumberFormatException ignored) {}

        try {
            foodTrackingWeekly = Integer.parseInt(nutritionGoalView.getText().toString().trim());
        } catch (NumberFormatException ignored) {}

        // Проверка на заполнение цели
        if (selectedGoal.isEmpty() || workoutsWeekly == 0 || foodTrackingWeekly == 0) {
            Toast.makeText(requireContext(), "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохраняем цель
        GeneralGoalDao goalDao = new GeneralGoalDao(MainActivity.getAppDataBase());

        GeneralGoalModel newGoal = new GeneralGoalModel(
                0, // id автоинкремент
                selectedGoal,
                workoutsWeekly,
                foodTrackingWeekly,
                formattedDate // текущая дата
        );

        goalDao.insertGoal(newGoal); // добавляем в базу

        //Возврат на предыдущий экран
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }
}