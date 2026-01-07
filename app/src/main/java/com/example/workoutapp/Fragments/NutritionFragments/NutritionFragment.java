package com.example.workoutapp.Fragments.NutritionFragments;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.NutritionAdapters.OutsideMealAdapter;
import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.Data.ProfileDao.DailyFoodTrackingDao;
import com.example.workoutapp.Data.Tables.AppDataBase;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Models.NutritionModels.MealNameModel;
import com.example.workoutapp.Models.ProfileModels.DailyFoodTrackingModel;
import com.example.workoutapp.R;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class NutritionFragment extends Fragment {

    private RecyclerView outer_RV;
    private OutsideMealAdapter outsideMealAdapter;
    private List<MealModel> mealList = new ArrayList<>();

    // Переносим инициализацию DAO в метод, чтобы гарантировать наличие БД
    private MealNameDao mealNameDao;
    private ConnectingMealDao connectingMealDao;
    private MealFoodDao foodMealDao;

    private ImageView imageView;
    private TextView text1, text2;
    private String currentFormattedDate;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition, container, false);

        // Инициализация DAO через MainActivity
        initDAOs();

        Button addMealBtn = view.findViewById(R.id.nutrition_fragment_add_meal_Btn);
        outer_RV = view.findViewById(R.id.nutrition_fragment_meal_RV);
        imageView = view.findViewById(R.id.nutrition_fragment_image_IV);
        text1 = view.findViewById(R.id.nutrition_fragment_title2_TV);
        text2 = view.findViewById(R.id.nutrition_fragment_hint2_TV);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentFormattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } else {
            currentFormattedDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new java.util.Date());
        }

        outsideMealAdapter = new OutsideMealAdapter(this, outer_RV);
        outer_RV.setLayoutManager(new LinearLayoutManager(requireContext()));
        outer_RV.setAdapter(outsideMealAdapter);

        updateMealList(currentFormattedDate);

        addMealBtn.setOnClickListener(v -> ShowDialogAddMeal(currentFormattedDate));

        setupDateHeader(view);

        getParentFragmentManager().setFragmentResultListener("preset_added_result", this, (key, bundle) -> {
            if (bundle.getBoolean("meal_preset_added", false)) {
                refreshMealData();
            }
        });

        return view;
    }

    private void initDAOs() {
        AppDataBase db = MainActivity.getAppDataBase();
        mealNameDao = new MealNameDao(db);
        connectingMealDao = new ConnectingMealDao(db);
        foodMealDao = new MealFoodDao(db);
    }

    private void setupDateHeader(View view) {
        TextView dateTextView = view.findViewById(R.id.nutrition_fragment_date_TV);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(calendar.getTime());
        if (!formattedDate.isEmpty()) {
            formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        }
        dateTextView.setText(formattedDate);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Если фрагмент не скрыт, обновляем данные
        if (!isHidden()) {
            refreshMealData();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // Срабатывает при переключении вкладок в BottomNavigationView (show/hide)
        if (!hidden) {
            refreshMealData();
        }
    }

    private void updateMealList(String date) {
        AppDataBase db = MainActivity.getAppDataBase();
        if (db == null || !db.isDbOpen()) return;

        mealList.clear();

        // Получаем данные асинхронно или проверяем безопасность
        List<Integer> mealIds = mealNameDao.getMealNamesIdsByDate(date);

        for (Integer mealId : mealIds) {
            MealNameModel mealName = mealNameDao.getMealNameModelById(mealId);
            if (mealName == null) continue;

            MealModel meal = new MealModel(
                    mealId,
                    mealName.getMeal_name(),
                    mealName.getMealData(),
                    foodMealDao.getMealFoodsByIds(connectingMealDao.getFoodIdsForMeal(mealId))
            );
            mealList.add(meal);
        }

        outsideMealAdapter.updateOuterAdapterList(mealList);
        togglePlaceholders();
        syncDailyTotals();
    }

    private void togglePlaceholders() {
        int visibility = mealList.isEmpty() ? View.VISIBLE : View.GONE;
        int invVisibility = mealList.isEmpty() ? View.GONE : View.VISIBLE; // для элементов, которые скрываем

        imageView.setVisibility(mealList.isEmpty() ? View.VISIBLE : View.GONE);
        text1.setVisibility(mealList.isEmpty() ? View.VISIBLE : View.GONE);
        text2.setVisibility(mealList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void syncDailyTotals() {
        AppDataBase db = MainActivity.getAppDataBase();
        if (db == null || !db.isDbOpen() || mealList.isEmpty()) return;

        int totalCalories = 0;
        float totalProtein = 0, totalFat = 0, totalCarbs = 0;

        for (MealModel meal : mealList) {
            for (FoodModel food : meal.getMeal_food_list()) {
                totalCalories += (int) food.getCalories();
                totalProtein += (float) food.getProtein();
                totalFat += (float) food.getFat();
                totalCarbs += (float) food.getCarb();
            }
        }

        DailyFoodTrackingDao historyDao = new DailyFoodTrackingDao(db);
        DailyFoodTrackingModel dailyRecord = new DailyFoodTrackingModel(0, totalCalories, totalProtein, totalFat, totalCarbs, currentFormattedDate);
        historyDao.insertOrUpdate(dailyRecord);
    }

    public void refreshMealData() {
        if (outer_RV != null) {
            updateMealList(currentFormattedDate);
        }
    }

    // Упрощенный метод без создания нового адаптера
    public void refreshAdapter() {
        mealList = new ArrayList<>();

        List<Integer> mealIds = mealNameDao.getMealNamesIdsByDate(currentFormattedDate);
        for (Integer mealId : mealIds) {
            MealNameModel mealName = mealNameDao.getMealNameModelById(mealId);
            if (mealName == null) continue;

            MealModel meal = new MealModel(
                    mealId,
                    mealName.getMeal_name(),
                    mealName.getMealData(),
                    foodMealDao.getMealFoodsByIds(
                            connectingMealDao.getFoodIdsForMeal(mealId)
                    )
            );

            mealList.add(new MealModel(meal));
        }

        outsideMealAdapter = new OutsideMealAdapter(NutritionFragment.this, outer_RV);
        outsideMealAdapter.updateOuterAdapterList(mealList);
        outer_RV.setAdapter(outsideMealAdapter);

        // Показ или скрытие заглушки
        if (!mealList.isEmpty()) {
            imageView.setVisibility(View.GONE);
            text1.setVisibility(View.GONE);
            text2.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            text1.setVisibility(View.VISIBLE);
            text2.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    private void ShowDialogAddMeal(String date) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.add_meal_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        ImageButton backBtn = dialog.findViewById(R.id.nutrition_close_D_BTN);
        EditText nameMeal_ET = dialog.findViewById(R.id.nutrition_name_D_ET);
        Button createMealBtn = dialog.findViewById(R.id.nutrition_create_D_BTN);
        Button goToPresetsBtn = dialog.findViewById(R.id.nutrition_presets_D_BTN);
        ConstraintLayout rootLayout = dialog.findViewById(R.id.add_meal_dialog_CL);
        TextView tvErrorName = dialog.findViewById(R.id.tv_error_name);

        backBtn.setOnClickListener(v -> dialog.dismiss());

        goToPresetsBtn.setOnClickListener(v -> {
            dialog.dismiss();
            openPresetsFragment();
        });

        rootLayout.setOnTouchListener((v, event) -> {
            nameMeal_ET.clearFocus();
            hideKeyboard(nameMeal_ET);
            return false;
        });

        createMealBtn.setOnClickListener(v -> {
            String mealName = nameMeal_ET.getText().toString().trim();
            if (mealName.isEmpty()) {
                tvErrorName.setText("Пожалуйста, введите название приёма пищи");
                tvErrorName.setVisibility(View.VISIBLE);
            } else {
                mealNameDao.insertMealName(mealName, date);
                updateMealList(date);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void openPresetsFragment() {
        Fragment selectionFragment = new SelectionMealPresetsFragment();
        getParentFragmentManager().beginTransaction()
                .hide(this)
                .add(R.id.frameLayout, selectionFragment, "selection_meal_preset")
                .addToBackStack(null)
                .commit();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateFoodInMeal(int mealId, FoodModel updatedFood) {
        for (MealModel meal : mealList) {
            if (meal.getMeal_name_id() == mealId) {
                meal.updateFood(updatedFood);
                break;
            }
        }
        outsideMealAdapter.notifyDataSetChanged();
        syncDailyTotals();
    }

    public void removeFoodFromMeal() {
        mealList.clear();
        // Получаем все ID приемов пищи на текущую дату
        List<Integer> mealIds = mealNameDao.getMealNamesIdsByDate(currentFormattedDate);
        for (Integer mealId : mealIds) {
            MealNameModel mealName = mealNameDao.getMealNameModelById(mealId);
            if (mealName == null) continue;
            String name = mealName.getMeal_name();
            String mealData = mealName.getMealData();
            // Создаем новый MealModel с текущим списком foodModels
            MealModel meal = new MealModel(mealId, name, mealData, foodMealDao.getMealFoodsByIds(connectingMealDao.getFoodIdsForMeal(mealId)));
            mealList.add(new MealModel(meal)); // Добавляем в новый список
        }
    }

    public OutsideMealAdapter getOutsideMealAdapter() {
        return outsideMealAdapter;
    }
}