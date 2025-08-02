package com.example.workoutapp.Fragments.NutritionFragments;

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
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Data.NutritionDao.ConnectingMealDao;
import com.example.workoutapp.Data.NutritionDao.MealFoodDao;
import com.example.workoutapp.Data.NutritionDao.MealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Adapters.NutritionAdapters.OutsideMealAdapter;
import com.example.workoutapp.Models.NutritionModels.FoodModel;
import com.example.workoutapp.Models.NutritionModels.MealModel;
import com.example.workoutapp.Models.NutritionModels.MealNameModel;
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
    private MealNameDao mealNameDao = new MealNameDao(MainActivity.getAppDataBase());
    private ConnectingMealDao connectingMealDao = new ConnectingMealDao(MainActivity.getAppDataBase());
    private MealFoodDao foodMealDao = new MealFoodDao(MainActivity.getAppDataBase());

    private ImageView imageView;
    private TextView text1;
    private TextView text2;
    private String currentFormattedDate;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View NutritionFragmentView = inflater.inflate(R.layout.fragment_nutrition, container, false);
        Button addMealBtn = NutritionFragmentView.findViewById(R.id.addMealBtn);
        outer_RV = NutritionFragmentView.findViewById(R.id.MealRecyclerView);
        imageView = NutritionFragmentView.findViewById(R.id.imageView);
        text1 = NutritionFragmentView.findViewById(R.id.textView1);
        text2 = NutritionFragmentView.findViewById(R.id.textView2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate currentDate = LocalDate.now();
            currentFormattedDate = currentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } else {
            currentFormattedDate = "";
        }



        outsideMealAdapter = new OutsideMealAdapter(NutritionFragment.this, outer_RV);

        outer_RV.setHasFixedSize(true);
        outer_RV.setLayoutManager(new LinearLayoutManager(requireContext()));
        outer_RV.setAdapter(outsideMealAdapter);

        updateMealList(currentFormattedDate);

        addMealBtn.setOnClickListener(v -> ShowDialogAddMeal(currentFormattedDate));

        // Настройка отображения даты
        TextView dateTextView = NutritionFragmentView.findViewById(R.id.dateTextNutrition);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(calendar.getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextView.setText(formattedDate);

        return NutritionFragmentView;
    }
    private void ShowDialogAddMeal(String data) {
        Dialog dialogAddMeal = new Dialog(requireContext());
        dialogAddMeal.setContentView(R.layout.add_meal_dialog);
        Objects.requireNonNull(dialogAddMeal.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogAddMeal.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialogAddMeal.show();

        ImageButton backBtn = dialogAddMeal.findViewById(R.id.imageButtonBack1);
        EditText nameMeal_ET = dialogAddMeal.findViewById(R.id.editText);
        Button createMealBtn = dialogAddMeal.findViewById(R.id.createWorkBtn);
        Button goToPresetsBtn = dialogAddMeal.findViewById(R.id.presetsMealBtn);

        // Обработчик кнопки "Назад"
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddMeal.dismiss();
            }
        });

        goToPresetsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddMeal.dismiss();
                replaceFragment(new SelectionMealPresetsFragment());
            }
        });

        createMealBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mealName = nameMeal_ET.getText().toString().trim();

                if (mealName.isEmpty()) {
                    Toast.makeText(requireContext(), "Пожалуйста, введите название приема пищи", Toast.LENGTH_SHORT).show();
                } else {
                    mealNameDao.insertMealName(mealName, data);
                    updateMealList(data);
                    mealNameDao.logAllMealNames();
                }

                dialogAddMeal.dismiss();
            }
        });
    }


    private void updateMealList(String data) {
        // Очищаем список перед добавлением новых данных
        mealList.clear();



        // Получаем все ID приемов пищи на текущую дату
        List<Integer> mealIds = mealNameDao.getMealNamesIdsByDate(data);

        for (Integer mealId : mealIds) {
            MealNameModel mealName = mealNameDao.getMealNameModelById(mealId);
            if (mealName == null) continue;

            String name = mealName.getMeal_name();
            String mealData = mealName.getMealData();

            // Создаем новый MealModel с текущим списком foodModels
            MealModel meal = new MealModel(mealId, name, mealData, foodMealDao.getMealFoodsByIds(connectingMealDao.getFoodIdsForMeal(mealId)));
            mealList.add(new MealModel(meal));  // Добавляем в новый список
        }

        // Обновляем адаптер с новым списком
        outsideMealAdapter.updateOuterAdapterList(mealList);

        if (!mealList.isEmpty()) {
            imageView.setVisibility(View.GONE);
            text1.setVisibility(View.GONE);
            text2.setVisibility(View.GONE);
        }
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
            mealList.add(new MealModel(meal));  // Добавляем в новый список
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    public void updateFoodInMeal(int mealId, FoodModel updatedFood) {
        for (MealModel meal : mealList) {
            if (meal.getMeal_name_id() == mealId) {
                meal.updateFood(updatedFood); // ты должен реализовать этот метод в MealModel
                break;
            }
        }

        // Обновляем только нужную карточку
        outsideMealAdapter.notifyDataSetChanged();
    }

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

    private void replaceFragment(Fragment newFragment) {

        mealList.clear();
        // Получаем менеджер фрагментов
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            // Начинаем транзакцию фрагментов
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Заменяем текущий фрагмент на новый
            fragmentTransaction.replace(R.id.frameLayout, newFragment);
            // Добавляем транзакцию в бэкстек (если нужно)
            fragmentTransaction.addToBackStack(null);
            // Выполняем транзакцию
            fragmentTransaction.commit();
        }
    }


    public OutsideMealAdapter getOutsideMealAdapter() {
        return outsideMealAdapter;
    }
}