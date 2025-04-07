package com.example.workoutapp.Workout;

import android.annotation.SuppressLint;
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

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        View rootLayout = workoutFragmentView.findViewById(R.id.fragment_root_layout); // Убедитесь, что добавили ID в XML

        // Устанавливаем обработчик касания на корневой контейнер
        /*rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Получаем текущий фокусированный элемент
                View focusedView = getActivity().getCurrentFocus();

                // Проверяем, был ли клик на пустой части экрана (не на редактируемом поле)
                if (focusedView instanceof EditText) {
                    // Если фокус на EditText и событие не является прокруткой, скрываем клавиатуру
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        // Скрыть клавиатуру, если было нажато на пустую область
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
                        // Убираем фокус с EditText
                        focusedView.clearFocus();
                        return true; // Событие было обработано
                    }
                }
                return false; // Событие передается дальше (т.е. прокрутка)
            }
        });*/



        exWorkoutRecyclerView = workoutFragmentView.findViewById(R.id.WorkoutRecyclerView);
        tempExModelList = tempDataBaseEx.getAllExercisesWithSets();
        outsideAdapter = new OutsideAdapter(WorkoutFragment.this, exWorkoutRecyclerView);

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
            outsideAdapter.saveAllInnerAdapters();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Сохраняем изменения в базу данных при уходе с фрагмента
        if (outsideAdapter != null) {
            outsideAdapter.saveAllInnerAdapters();
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