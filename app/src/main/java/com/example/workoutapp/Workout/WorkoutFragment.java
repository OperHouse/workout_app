package com.example.workoutapp.Workout;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.OutsideAdapter;
import com.example.workoutapp.DAO.CompletedWorkoutDao;
import com.example.workoutapp.DAO.TempWorkoutDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.TempExModel;
import com.example.workoutapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkoutFragment extends Fragment {

    TempWorkoutDao TempWorkDao;
    CompletedWorkoutDao CompletedDao;
    List<TempExModel> tempExModelList;
    OutsideAdapter outsideAdapter;
    RecyclerView exWorkoutRecyclerView;
    private Button finalWorkoutBtn;
    private TextView text1, text2;
    private ImageView image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.TempWorkDao = new TempWorkoutDao(MainActivity.getAppDataBase());
        this.CompletedDao = new CompletedWorkoutDao(MainActivity.getAppDataBase());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Инфлейтим разметку фрагмента
        View workoutFragmentView = inflater.inflate(R.layout.fragment_workout, container, false);

        exWorkoutRecyclerView = workoutFragmentView.findViewById(R.id.WorkoutRecyclerView);
        tempExModelList = TempWorkDao.getAllTempExercisesWithSets();
        outsideAdapter = new OutsideAdapter(WorkoutFragment.this, exWorkoutRecyclerView);

        exWorkoutRecyclerView.setHasFixedSize(true);
        exWorkoutRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        exWorkoutRecyclerView.setAdapter(outsideAdapter);

        outsideAdapter.updateExList(tempExModelList);
        // Находим кнопку для добавления упражнения
        Button addExBtn = workoutFragmentView.findViewById(R.id.addExBtn);
        finalWorkoutBtn = workoutFragmentView.findViewById(R.id.finalWorkBtn);
        text1 = workoutFragmentView.findViewById(R.id.textView1);
        text2 = workoutFragmentView.findViewById(R.id.textView2);
        image = workoutFragmentView.findViewById(R.id.imageView);

        setVisibility(finalWorkoutBtn,text1,text2,image);


        finalWorkoutBtn.setOnClickListener(v -> {
            showDialogConfirm();
        });

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

    private void showDialogConfirm() {
        Dialog dialogCreateEx = new Dialog(requireContext());
        dialogCreateEx.setContentView(R.layout.confirm_dialog_layout);
        Objects.requireNonNull(dialogCreateEx.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        AtomicBoolean isDialogClosedByOutsideClick = new AtomicBoolean(false);
        // Разрешаем закрытие диалога при нажатии вне его
        dialogCreateEx.setCancelable(true); // Делаем диалог закрываемым
        dialogCreateEx.setCanceledOnTouchOutside(true); // Закрыть при клике вне диалога

        Button deleteBtn = dialogCreateEx.findViewById(R.id.btnDelete);
        Button chanelBtn = dialogCreateEx.findViewById(R.id.btnChanel);
        TextView text1 = dialogCreateEx.findViewById(R.id.textView);
        TextView text2 = dialogCreateEx.findViewById(R.id.text1);

        deleteBtn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.lime));
        deleteBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        deleteBtn.setText("Подтвердить");
        text1.setText("Завершение тренировки");
        text2.setText("Вы действительно хотите завершить тренировку?");


        if(dialogCreateEx.getWindow() != null){
            dialogCreateEx.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        chanelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCreateEx.dismiss();

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outsideAdapter.saveAllInnerAdapters();
                CompletedDao.insertCompletedWorkouts(TempWorkDao.extractCompletedWorkoutsFromTemp());
                TempWorkDao.clearTempWorkoutData();
                refreshAdapter();
                dialogCreateEx.dismiss();
            }
        });



        // Слушаем закрытие по клику вне
        dialogCreateEx.setOnCancelListener(dialog -> isDialogClosedByOutsideClick.set(true));
        dialogCreateEx.show();
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
    public void refreshAdapter() {
        tempExModelList = TempWorkDao.getAllTempExercisesWithSets();
        outsideAdapter = new OutsideAdapter(WorkoutFragment.this, exWorkoutRecyclerView);
        outsideAdapter.updateExList(tempExModelList);
        exWorkoutRecyclerView.setAdapter(outsideAdapter);
        setVisibility(finalWorkoutBtn, text1, text2, image);
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

    private void setVisibility(Button b, TextView t1, TextView t2, ImageView im){
        if (tempExModelList.isEmpty()) {
            // Показываем заглушку (тексты и изображение), скрываем кнопку
            finalWorkoutBtn.setVisibility(View.GONE);
            t1.setVisibility(View.VISIBLE);
            t2.setVisibility(View.VISIBLE);
            im.setVisibility(View.VISIBLE);
        } else {
            // Скрываем заглушку, показываем кнопку
            finalWorkoutBtn.setVisibility(View.VISIBLE);
            t1.setVisibility(View.GONE);
            t2.setVisibility(View.GONE);
            im.setVisibility(View.GONE);
        }
    }

}