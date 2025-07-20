package com.example.workoutapp.NutritionFragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.OutsideAdapter;
import com.example.workoutapp.NutritionAdapters.OutsideMealAdapter;
import com.example.workoutapp.R;
import com.example.workoutapp.WorkoutFragments.WorkoutFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


public class NutritionFragment extends Fragment {

    private RecyclerView outer_RV;
    private OutsideMealAdapter outsideMealAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View NutritionFragmentView = inflater.inflate(R.layout.fragment_nutrition, container, false);
        Button addMealBtn = NutritionFragmentView.findViewById(R.id.addMealBtn);
        outer_RV = NutritionFragmentView.findViewById(R.id.MealRecyclerView);


        outsideMealAdapter = new OutsideMealAdapter(NutritionFragment.this, outer_RV);

        outer_RV.setHasFixedSize(true);
        outer_RV.setLayoutManager(new LinearLayoutManager(requireContext()));
        outer_RV.setAdapter(outsideMealAdapter);

        outsideMealAdapter.updateOuterAdapterList();


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