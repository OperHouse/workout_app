package com.example.workoutapp.Workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.OutsideAdapter;
import com.example.workoutapp.Models.TempExModel;
import com.example.workoutapp.R;
import com.example.workoutapp.TempDataBaseEx;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkoutFragment extends Fragment {

    TempDataBaseEx tempDataBaseEx;
    List<TempExModel> tempExModelList;
    OutsideAdapter outsideAdapter;
    RecyclerView exWorkoutRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tempDataBaseEx = new TempDataBaseEx(requireContext());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Инфлейтим разметку фрагмента
        View workoutFragmentView = inflater.inflate(R.layout.fragment_workout, container, false);


        exWorkoutRecyclerView = workoutFragmentView.findViewById(R.id.WorkoutRecyclerView);
        tempExModelList = tempDataBaseEx.getAllExercisesWithSets();
        outsideAdapter = new OutsideAdapter(WorkoutFragment.this);

        exWorkoutRecyclerView.setHasFixedSize(true);
        exWorkoutRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        exWorkoutRecyclerView.setAdapter(outsideAdapter);

        outsideAdapter.updateExList(tempExModelList);
        // Находим кнопку для добавления упражнения
        Button addExBtn = workoutFragmentView.findViewById(R.id.addExBtn);

        // Устанавливаем обработчик нажатия на кнопку
        addExBtn.setOnClickListener(v -> {
            // Заменяем текущий фрагмент на FullscreenFragment
            replaceFragment(new AddExFragment());
        });

        // Настройка отображения даты
        TextView dateTextView = workoutFragmentView.findViewById(R.id.dateTextWorkout);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(calendar.getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextView.setText(formattedDate);

        return workoutFragmentView;
    }
    @Override
    public void onPause() {
        super.onPause();

        // Сохраняем изменения в базу данных при уходе с фрагмента
        if (outsideAdapter != null) {
            outsideAdapter.saveChangesToDatabase();
        }
    }
    // Метод для замены фрагмента
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