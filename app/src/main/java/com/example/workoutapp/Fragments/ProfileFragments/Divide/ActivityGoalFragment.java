package com.example.workoutapp.Fragments.ProfileFragments.Divide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.workoutapp.Data.ProfileDao.ActivityGoalDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ProfileModels.ActivityGoalModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.OnNavigationVisibilityListener;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityGoalFragment extends Fragment {

    private OnNavigationVisibilityListener navigationListener;

    private EditText caloriesToBurnEdit, amountStepsEdit;
    private MaterialButton saveBtn;

    public ActivityGoalFragment() {
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
        View view = inflater.inflate(R.layout.fragment_activity_goal, container, false);

        // Инициализация элементов
        ImageButton imageButtonBack = view.findViewById(R.id.imageButtonBack);
        saveBtn = view.findViewById(R.id.buttonSave);
        // Используем id из fragment_activity_goal.xml (предполагаем, что они такие)
        caloriesToBurnEdit = view.findViewById(R.id.editTextCaloriesToBurn);
        amountStepsEdit = view.findViewById(R.id.editTextAmountSteps);

        // Кнопка "Назад"
        imageButtonBack.setOnClickListener(view1 -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // 🔹 Скрытие клавиатуры по клику на пустое место (Функционал фокуса)
        ConstraintLayout rootLayout = view.findViewById(R.id.rootConstraintLayout);
        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                hideKeyboardAndClearFocus();
            }
            // Всегда возвращаем false, чтобы касание "прошло" к дочерним элементам
            return false;
        });

        // 🔹 Обработка кнопки "Готово"/"Далее" на клавиатуре
        setupEditorActionListener(caloriesToBurnEdit);
        setupEditorActionListener(amountStepsEdit);

        // Кнопка сохранения
        saveBtn.setOnClickListener(v -> saveActivityGoal());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadActivityGoalData(); // Подгружаем данные из БД
    }

    // 🔹 Настройка слушателя для клавиатуры
    private void setupEditorActionListener(EditText editText) {
        editText.setOnEditorActionListener((v, actionId, event) -> {
            // Срабатывает при нажатии "Готово" или "Далее"
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT) {
                hideKeyboardAndClearFocus();
                return true;
            }
            return false;
        });
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
    private void loadActivityGoalData() {
        ActivityGoalDao goalDao = new ActivityGoalDao(MainActivity.getAppDataBase());
        ActivityGoalModel lastGoal = goalDao.getLatestGoal();

        if (lastGoal != null) {
            if (lastGoal.getCaloriesToBurn() > 0) {
                caloriesToBurnEdit.setText(String.valueOf(lastGoal.getCaloriesToBurn()));
            }
            if (lastGoal.getStepsGoal() > 0) {
                amountStepsEdit.setText(String.valueOf(lastGoal.getStepsGoal()));
            }
        }
    }

    // 🔹 Сохранение данных
    private void saveActivityGoal() {
        int caloriesToBurn = 0;
        int amountSteps = 0;
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(new Date(currentTimeMillis));

        // Конвертация строковых значений в int
        try {
            caloriesToBurn = Integer.parseInt(caloriesToBurnEdit.getText().toString().trim());
        } catch (NumberFormatException ignored) {}

        try {
            amountSteps = Integer.parseInt(amountStepsEdit.getText().toString().trim());
        } catch (NumberFormatException ignored) {}

        // Проверка на заполнение полей
        if (caloriesToBurn == 0 || amountSteps == 0) {
            Toast.makeText(requireContext(), "Пожалуйста, заполните все поля.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохраняем цель активности
        ActivityGoalDao goalDao = new ActivityGoalDao(MainActivity.getAppDataBase());

        ActivityGoalModel newGoal = new ActivityGoalModel(
                0, // id автоинкремент
                formattedDate,
                amountSteps,
                caloriesToBurn
        );

        goalDao.addGoal(newGoal); // добавляем в базу

        Toast.makeText(requireContext(), "Цели активности сохранены.", Toast.LENGTH_SHORT).show();

        //Возврат на предыдущий экран
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }
}