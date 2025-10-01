package com.example.workoutapp.Fragments.WorkoutFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.WorkoutAdapters.OutsideAdapter;
import com.example.workoutapp.Data.WorkoutDao.CARDIO_SET_DETAILS_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.STRENGTH_SET_DETAILS_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_EXERCISE_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.CardioSetModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.Models.WorkoutModels.StrengthSetModel;
import com.example.workoutapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WorkoutFragment extends Fragment {


    private OutsideAdapter workoutAdapter;
    private RecyclerView workoutRecyclerView;
    private List<ExerciseModel> exList;
    private WORKOUT_EXERCISE_TABLE_DAO workoutExerciseTableDao;
    private CARDIO_SET_DETAILS_TABLE_DAO cardioSetDetailsTableDao;
    private STRENGTH_SET_DETAILS_TABLE_DAO strengthSetDetailsTableDao;

    public WorkoutFragment() {
        // Required empty public constructor
    }

    public void setExercises(List<ExerciseModel> exercises) {
        this.exList = exercises; // просто сохраняем ссылку
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        TextView dateTextWorkout = view.findViewById(R.id.dateTextWorkout);
        Button addExBtn = view.findViewById(R.id.addExBtn);
        Button finalWorkBtn = view.findViewById(R.id.finalWorkBtn);




        // Инициализируем recyclerView
        workoutRecyclerView = view.findViewById(R.id.WorkoutRecyclerView);
        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        workoutRecyclerView.setItemAnimator(null);

        workoutAdapter = new OutsideAdapter(this);
        workoutAdapter.updateExList(exList);
        workoutRecyclerView.setAdapter(workoutAdapter);

        updateUI(view, exList);

        workoutAdapter.setOnExerciseListChangedListener(newList -> updateUI(view, newList));
        addExBtn.setOnClickListener(v -> {
            Fragment selectionFragment = new Selection_Ex_Preset_Fragment();
            FragmentManager fragmentManager = getParentFragmentManager(); // или getFragmentManager()
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction
                    .hide(this)  // Скрываем текущий WorkoutFragment
                    .add(R.id.frameLayout, selectionFragment, "selection_ex") // Добавляем новый фрагмент с тегом
                    .addToBackStack(null)  // Чтобы можно было вернуться назад
                    .commit();
        });

        finalWorkBtn.setOnClickListener(v -> {
            workoutExerciseTableDao = new WORKOUT_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
            cardioSetDetailsTableDao = new CARDIO_SET_DETAILS_TABLE_DAO(MainActivity.getAppDataBase());
            strengthSetDetailsTableDao = new STRENGTH_SET_DETAILS_TABLE_DAO(MainActivity.getAppDataBase());

            for (ExerciseModel elm: exList) {
                if (elm.getSets().isEmpty()) {
                    workoutExerciseTableDao.deleteExercise(elm);
                    continue;
                }

                List<Object> updatedSets = new ArrayList<>(); // временный список для актуальных сетов
                int orderCounter = 1;
                boolean exerciseDeleted = false; // Флаг, чтобы отслеживать, было ли удалено упражнение

                for (Object set : elm.getSets()) {
                    boolean isEmpty = false;

                    if (set instanceof StrengthSetModel) {
                        StrengthSetModel strengthSet = (StrengthSetModel) set;
                        // Проверяем, если хотя бы одно поле пустое или равно нулю
                        isEmpty = (strengthSet.getRep() == 0 || strengthSet.getWeight() == 0);
                    } else if (set instanceof CardioSetModel) {
                        CardioSetModel cardioSet = (CardioSetModel) set;
                        // Проверяем, если хотя бы одно поле пустое или равно нулю
                        isEmpty = (cardioSet.getTemp() == 0 || cardioSet.getTime() == 0 || cardioSet.getDistance() == 0);
                    }

                    if (isEmpty) {
                        if (set instanceof StrengthSetModel) {
                            StrengthSetModel strengthSet = (StrengthSetModel) set;
                            strengthSetDetailsTableDao.deleteStrengthSet(strengthSet);
                            updatedSets.remove(strengthSet);
                            if (updatedSets.isEmpty()) {
                                workoutExerciseTableDao.deleteExercise(elm);
                                exerciseDeleted = true; // Помечаем, что упражнение удалено
                            }
                        } else if (set instanceof CardioSetModel) {
                            CardioSetModel cardioSet = (CardioSetModel) set;
                            cardioSetDetailsTableDao.deleteCardioSet(cardioSet);
                            updatedSets.remove(cardioSet);
                            if (updatedSets.isEmpty()) {
                                workoutExerciseTableDao.deleteExercise(elm);
                                exerciseDeleted = true; // Помечаем, что упражнение удалено
                            }
                        }
                    } else {
                        // обновить порядок
                        if (set instanceof StrengthSetModel) {
                            ((StrengthSetModel) set).setOrder(orderCounter);
                            strengthSetDetailsTableDao.updateSetOrder((StrengthSetModel) set);
                        } else if (set instanceof CardioSetModel) {
                            ((CardioSetModel) set).setOrder(orderCounter);
                            cardioSetDetailsTableDao.updateSetOrder((CardioSetModel) set);
                        }
                        orderCounter++;
                        updatedSets.add(set);
                    }
                }

                // Если упражнение не было удалено, то оно становится завершённым
                if (!exerciseDeleted) {
                    workoutExerciseTableDao.markExerciseAsFinished(elm.getExercise_id()); // Помечаем как завершённое
                }
            }

            // Обновляем список упражнений, чтобы отобразить завершённые
            exList = workoutExerciseTableDao.getExByState("unfinished");
            assert getView() != null;
            updateUI(getView(), exList);
            workoutAdapter.updateExList(exList);
            Toast.makeText(requireContext(), "Тренировка завершена!", Toast.LENGTH_SHORT).show();
        });

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", new Locale("ru", "RU"));
        String formattedDate = dateFormat.format(calendar.getTime());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextWorkout.setText(formattedDate);

        return view;
    }

    private void updateUI(View rootView, List<ExerciseModel> list) {
        TextView textView1 = rootView.findViewById(R.id.textView1);
        TextView textView2 = rootView.findViewById(R.id.textView2);
        ImageView image = rootView.findViewById(R.id.imageView);
        Button finalWorkBtn = rootView.findViewById(R.id.finalWorkBtn);

        if (list == null || list.isEmpty()) {
            textView1.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
            image.setVisibility(View.VISIBLE);
            finalWorkBtn.setVisibility(View.GONE);
            if (workoutRecyclerView != null) workoutRecyclerView.setVisibility(View.GONE);
        } else {
            textView1.setVisibility(View.GONE);
            textView2.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            finalWorkBtn.setVisibility(View.VISIBLE);
            if (workoutRecyclerView != null) workoutRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void refreshWorkoutData() {
        if (workoutAdapter != null) {
            workoutAdapter.updateExList(exList); // из setExercises()
            assert getView() != null;
            updateUI(getView(), exList);
        }
    }
}