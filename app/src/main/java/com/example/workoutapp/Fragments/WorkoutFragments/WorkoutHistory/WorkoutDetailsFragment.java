package com.example.workoutapp.Fragments.WorkoutFragments.WorkoutHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.Models.WorkoutModels.WorkoutSessionModel;
import com.example.workoutapp.R;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class WorkoutDetailsFragment extends Fragment {

    private WorkoutSessionModel session;

    public static WorkoutDetailsFragment newInstance(WorkoutSessionModel session) {
        WorkoutDetailsFragment fragment = new WorkoutDetailsFragment();
        fragment.session = session; // В идеале использовать Bundle + Parcelable/Serializable
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        displayStats(view);
    }

    private void initViews(View view) {
        view.findViewById(R.id.details_back_btn).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView titleTv = view.findViewById(R.id.details_title_TV);
        if (session != null) titleTv.setText(session.getWorkoutTitle());

        RecyclerView rv = view.findViewById(R.id.rv_details_exercises);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // ПЕРЕДАЕМ СПИСОК УПРАЖНЕНИЙ В АДАПТЕР
        if (session != null && session.getExercises() != null) {
            ExerciseDetailsAdapter adapter = new ExerciseDetailsAdapter(session.getExercises());
            rv.setAdapter(adapter);
        }
    }

    private void displayStats(View view) {
        if (session == null) return;

        TextView tvExCount = view.findViewById(R.id.stat_exercises_count);
        TextView tvTotalWeight = view.findViewById(R.id.stat_total_weight);
        TextView tvDate = view.findViewById(R.id.stat_date);

        tvExCount.setText(String.valueOf(session.getExercises().size()));

        // --- ОБРАБОТКА ДАТЫ ---
        try {
            String rawDate = session.getWorkoutDate(); // Предполагаем формат "yyyy-MM-dd"
            // Создаем форматтер для чтения даты из сессии
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            // Создаем форматтер для вывода (только день и месяц)
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());

            java.util.Date date = inputFormat.parse(rawDate);
            if (date != null) {
                tvDate.setText(outputFormat.format(date));
            }
        } catch (Exception e) {
            // Если что-то пошло не так (неверный формат), выводим как есть или заглушку
            tvDate.setText(session.getWorkoutDate());
        }

        // --- ПОДСЧЕТ ВЕСА ---
        double totalWeight = 0;
        for (ExerciseModel ex : session.getExercises()) {
            if (ex.getSets() != null) {
                for (Object set : ex.getSets()) {
                    if (set instanceof StrengthSetModel) {
                        StrengthSetModel s = (StrengthSetModel) set;
                        totalWeight += (s.getWeight() * s.getRep());
                    }
                }
            }
        }

        // Если вес целый, убираем лишние нули после запятой для экономии места
        if (totalWeight % 1 == 0) {
            tvTotalWeight.setText(String.format(Locale.getDefault(), "%.0f", totalWeight));
        } else {
            tvTotalWeight.setText(String.format(Locale.getDefault(), "%.1f", totalWeight));
        }
    }
}