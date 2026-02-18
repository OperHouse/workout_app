package com.example.workoutapp.Fragments.NutritionFragments.NutritionHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.R;

import java.util.Locale;

public class NutritionDetailsFragment extends Fragment {

    private static final String ARG_SESSION = "nutrition_session";
    private NutritionSessionModel session;

    public static NutritionDetailsFragment newInstance(NutritionSessionModel session) {
        NutritionDetailsFragment fragment = new NutritionDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SESSION, session);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            session = (NutritionSessionModel) getArguments().getSerializable(ARG_SESSION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nutrition_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
    }

    private void initViews(View view) {
        // Кнопка назад
        ImageView backBtn = view.findViewById(R.id.nutr_details_back_btn);
        backBtn.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Поля заголовка
        TextView tvDate = view.findViewById(R.id.tv_details_date);
        TextView tvKcal = view.findViewById(R.id.tv_details_total_kcal);
        TextView tvProt = view.findViewById(R.id.tv_details_prot);
        TextView tvFat = view.findViewById(R.id.tv_details_fat);
        TextView tvCarb = view.findViewById(R.id.tv_details_carb);

        // Установка данных сводки
        if (session != null) {
            tvDate.setText(session.getDate());
            tvKcal.setText(String.format(Locale.getDefault(), "%,.0f ккал", session.getTotalCalories()));
            tvProt.setText(String.format(Locale.getDefault(), "Белки\n%.0f г", session.getTotalProtein()));
            tvFat.setText(String.format(Locale.getDefault(), "Жиры\n%.0f г", session.getTotalFat()));
            tvCarb.setText(String.format(Locale.getDefault(), "Углеводы\n%.0f г", session.getTotalCarbs()));

            // Настройка списка приемов пищи
            RecyclerView rvMeals = view.findViewById(R.id.rv_meal_details_list);
            rvMeals.setLayoutManager(new LinearLayoutManager(getContext()));
            MealGroupAdapter adapter = new MealGroupAdapter(session.getMeals());
            rvMeals.setAdapter(adapter);
        }
    }
}