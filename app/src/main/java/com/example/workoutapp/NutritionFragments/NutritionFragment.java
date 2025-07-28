package com.example.workoutapp.NutritionFragments;

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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.DAO.ConnectingMealDao;
import com.example.workoutapp.DAO.MealFoodDao;
import com.example.workoutapp.DAO.MealNameDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.NutritionAdapters.OutsideMealAdapter;
import com.example.workoutapp.NutritionModels.MealNameModel;
import com.example.workoutapp.NutritionModels.MealModel;
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View NutritionFragmentView = inflater.inflate(R.layout.fragment_nutrition, container, false);
        Button addMealBtn = NutritionFragmentView.findViewById(R.id.addMealBtn);
        outer_RV = NutritionFragmentView.findViewById(R.id.MealRecyclerView);
        ImageView imageView = NutritionFragmentView.findViewById(R.id.imageView);
        TextView text1 = NutritionFragmentView.findViewById(R.id.textView1);
        TextView text2 = NutritionFragmentView.findViewById(R.id.textView2);




        String formattedDate1 = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate currentDate = LocalDate.now();
            formattedDate1 = currentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }

        List<Integer> mealIds = mealNameDao.getMealNamesIdsByDate(formattedDate1);

        for (Integer mealId : mealIds) {
            MealNameModel mealName = mealNameDao.getMealNameModelById(mealId);
            if (mealName == null) continue;

            String name = mealName.getMeal_name();
            String mealData = mealName.getMealData();


            // Создаем новый MealModel с текущим списком eatModels
            MealModel meal = new MealModel(mealId, name, mealData, foodMealDao.getMealFoodsByIds(connectingMealDao.getFoodIdsForMeal(mealId)));
            mealList.add(meal);
        }



        if(!mealList.isEmpty()){
            imageView.setVisibility(View.GONE);
            text1.setVisibility(View.GONE);
            text2.setVisibility(View.GONE);
        }

        outsideMealAdapter = new OutsideMealAdapter(NutritionFragment.this, outer_RV);

        outer_RV.setHasFixedSize(true);
        outer_RV.setLayoutManager(new LinearLayoutManager(requireContext()));
        outer_RV.setAdapter(outsideMealAdapter);

        outsideMealAdapter.updateOuterAdapterList(mealList);


        addMealBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialogAddMeal();
            }
        });


        // Настройка отображения даты
        TextView dateTextView = NutritionFragmentView.findViewById(R.id.dateTextNutrition);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(calendar.getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextView.setText(formattedDate);

        return NutritionFragmentView;
    }
    private void ShowDialogAddMeal() {
        Dialog dialogAddMeal = new Dialog(requireContext());
        dialogAddMeal.setContentView(R.layout.add_meal_dialog);
        Objects.requireNonNull(dialogAddMeal.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogAddMeal.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialogAddMeal.show();

        ImageButton backBtn = dialogAddMeal.findViewById(R.id.imageButtonBack1);
        EditText nameMeal = dialogAddMeal.findViewById(R.id.editText);
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

                //Заглушка
                //Тут должна быть логика добавления ПУСТОГО ПРИЕМА ПИЩИ
                dialogAddMeal.dismiss();
            }
        });
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


}