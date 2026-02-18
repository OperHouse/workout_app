package com.example.workoutapp.Fragments.NutritionFragments.NutritionHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealNameModel;
import com.example.workoutapp.R;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import net.sqlcipher.database.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NutritionHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private NutritionHistoryAdapter adapter;
    private final List<NutritionSessionModel> nutritionList = new ArrayList<>();

    private LinearLayout emptyLayout;
    private TextView tvNoData;
    private LottieAnimationView lottieEmpty;
    private Button loadMoreBtn;
    private Button showAllBtn;

    private MealNameDao mealNameDao;
    private ConnectingMealDao connectingMealDao;
    private MealFoodDao mealFoodDao;

    private int currentOffset = 0;
    private final int LIMIT = 10;
    private boolean isFilteredByDate = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nutrition_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SQLiteDatabase db = MainActivity.getAppDataBase();
        mealNameDao = new MealNameDao(db);
        connectingMealDao = new ConnectingMealDao(db);
        mealFoodDao = new MealFoodDao(db);

        initViews(view);
        loadNutritionData(true);
    }

    private void initViews(View view) {
        view.findViewById(R.id.nutr_history_back_btn).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        view.findViewById(R.id.nutr_calendar_btn).setOnClickListener(v -> showDatePicker());

        recyclerView = view.findViewById(R.id.rv_nutrition_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadMoreBtn = view.findViewById(R.id.nutr_load_more_BTN);
        showAllBtn = view.findViewById(R.id.nutr_show_all_BTN);
        loadMoreBtn.setOnClickListener(v -> {
            if (isFilteredByDate) {
                isFilteredByDate = false;
                loadNutritionData(true); // Сброс фильтра
            } else {
                loadNutritionData(false); // Загрузка следующей страницы
            }
        });

        showAllBtn.setOnClickListener(v -> resetFilter());

        adapter = new NutritionHistoryAdapter(nutritionList, session -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, NutritionDetailsFragment.newInstance(session))
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        emptyLayout = view.findViewById(R.id.nutr_empty_layout);
        tvNoData = view.findViewById(R.id.tv_nutr_no_data);
        lottieEmpty = view.findViewById(R.id.nutr_lottie_empty);
    }

    private void loadNutritionData(boolean isFirstLoad) {
        if (isFirstLoad) {
            currentOffset = 0;
            nutritionList.clear();
        }

        new Thread(() -> {
            // Передаем текущий LIMIT и OFFSET в запрос
            List<NutritionSessionModel> data = fetchHistoryFromDb(LIMIT, currentOffset);

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (isFirstLoad && data.isEmpty()) {
                        showEmptyState("Вы еще не записывали приемы пищи");
                    } else {
                        hideEmptyState();
                        nutritionList.addAll(data);
                        adapter.notifyDataSetChanged();

                        // Управление кнопкой "Загрузить еще"
                        if (data.size() < LIMIT) {
                            loadMoreBtn.setVisibility(View.GONE);
                        } else {
                            loadMoreBtn.setVisibility(View.VISIBLE);
                            loadMoreBtn.setText("Загрузить еще");
                        }
                        currentOffset += LIMIT;
                    }
                });
            }
        }).start();
    }

    private List<NutritionSessionModel> fetchHistoryFromDb(int limit, int offset) {
        List<NutritionSessionModel> history = new ArrayList<>();
        SQLiteDatabase db = MainActivity.getAppDataBase();

        // SQL запрос с пагинацией
        String query = "SELECT DISTINCT " + AppDataBase.MEAL_DATA +
                " FROM " + AppDataBase.MEAL_NAME_TABLE +
                " ORDER BY " + AppDataBase.MEAL_DATA + " DESC " +
                " LIMIT " + limit + " OFFSET " + offset;

        android.database.Cursor cursor = db.rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String date = cursor.getString(0);
                history.add(assembleSessionForDate(date));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return history;
    }

    private List<NutritionSessionModel> fetchHistoryForDate(String date) {
        List<NutritionSessionModel> history = new ArrayList<>();
        SQLiteDatabase db = MainActivity.getAppDataBase();

        String query = "SELECT DISTINCT " + AppDataBase.MEAL_DATA +
                " FROM " + AppDataBase.MEAL_NAME_TABLE +
                " WHERE " + AppDataBase.MEAL_DATA + " = ?";

        android.database.Cursor cursor = db.rawQuery(query, new String[]{date});
        try {
            if (cursor.moveToFirst()) {
                history.add(assembleSessionForDate(date));
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return history;
    }

    private NutritionSessionModel assembleSessionForDate(String date) {
        List<Integer> mealIds = mealNameDao.getMealNamesIdsByDate(date);
        List<MealWithFoods> mealsForDay = new ArrayList<>();

        for (Integer mealId : mealIds) {
            MealNameModel nameModel = mealNameDao.getMealNameModelById(mealId);
            List<Long> foodIds = connectingMealDao.getFoodIdsForMeal(mealId);
            List<FoodModel> foods = mealFoodDao.getMealFoodsByIds(foodIds);
            mealsForDay.add(new MealWithFoods(nameModel, foods));
        }
        return new NutritionSessionModel(date, mealsForDay);
    }

    private void showDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTheme(R.style.CustomCalendarTheme)
                .setTitleText("Поиск питания по дате")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraints)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            // Проверьте формат dd.MM.yyyy на соответствие вашей базе данных
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            loadSpecificDay(sdf.format(calendar.getTime()));
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void loadSpecificDay(String date) {
        isFilteredByDate = true;
        nutritionList.clear();
        adapter.notifyDataSetChanged();

        new Thread(() -> {
            List<NutritionSessionModel> data = fetchHistoryForDate(date);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (data.isEmpty()) {
                        // ЕСЛИ НИЧЕГО НЕ НАЙДЕНО
                        showEmptyState("Записей за " + date + " не найдено");
                        showAllBtn.setVisibility(View.VISIBLE); // Показываем кнопку в центре
                        loadMoreBtn.setVisibility(View.GONE);  // Прячем нижнюю кнопку загрузки
                    } else {
                        // ЕСЛИ ДАННЫЕ ЕСТЬ
                        hideEmptyState();
                        nutritionList.addAll(data);
                        adapter.notifyDataSetChanged();

                        // Прячем центральную кнопку, показываем нижнюю с текстом "Показать все"
                        showAllBtn.setVisibility(View.GONE);
                        loadMoreBtn.setVisibility(View.VISIBLE);
                        loadMoreBtn.setText("Показать все");
                    }
                });
            }
        }).start();
    }

    private void showEmptyState(String message) {
        emptyLayout.setVisibility(View.VISIBLE);
        lottieEmpty.setVisibility(View.VISIBLE); // Обязательно делаем видимой!
        lottieEmpty.playAnimation();
        tvNoData.setText(message);

        // Скрываем список, чтобы анимация была по центру
        recyclerView.setVisibility(View.GONE);
        loadMoreBtn.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyLayout.setVisibility(View.GONE);
        lottieEmpty.cancelAnimation();
        recyclerView.setVisibility(View.VISIBLE);
    }

    // Метод для сброса фильтра (когда нажали "Показать все")
    private void resetFilter() {
        isFilteredByDate = false;
        showAllBtn.setVisibility(View.GONE);
        loadMoreBtn.setText("Загрузить еще");
        loadNutritionData(true); // Загружаем общий список заново
    }
}