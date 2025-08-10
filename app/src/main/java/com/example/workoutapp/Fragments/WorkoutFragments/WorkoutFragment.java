package com.example.workoutapp.Fragments.WorkoutFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.WorkoutAdapters.OutsideAdapter;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.CONNECTING_WORKOUT_PRESET_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutFragment extends Fragment {

    private BASE_EXERCISE_TABLE_DAO baseExerciseTableDao;
    private WORKOUT_PRESET_NAME_TABLE_DAO workoutPresetNameTableDao;
    private CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingWorkoutPresetTableDao;

    private List<BaseExModel> exList;
    private OutsideAdapter exAdapter;
    private RecyclerView workoutRecyclerView;

    public WorkoutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализация DAO-классов
        baseExerciseTableDao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
        workoutPresetNameTableDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());
        connectingWorkoutPresetTableDao = new CONNECTING_WORKOUT_PRESET_TABLE_DAO(MainActivity.getAppDataBase());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        TextView dateTextWorkout = view.findViewById(R.id.dateTextWorkout);
        Button addExBtn = view.findViewById(R.id.addExBtn);
        Button finalWorkBtn = view.findViewById(R.id.finalWorkBtn);
        workoutRecyclerView = view.findViewById(R.id.WorkoutRecyclerView);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateTextWorkout.setText(currentDate);

        exList = baseExerciseTableDao.getAllExercises(); // если у тебя есть метод в DAO
        if (exList == null) {
            exList = new ArrayList<>(); // чтобы не было null
        }

        // ✅ Передаём view в updateUI
        updateUI(view, exList);

        workoutRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        exAdapter = new OutsideAdapter(this);
        workoutRecyclerView.setAdapter(exAdapter);

        addExBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = getFragmentManager();
            assert fragmentManager != null;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, new Selection_Ex_Preset_Fragment());
            fragmentTransaction.commit();
        });

        finalWorkBtn.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Тренировка завершена!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }


    private void updateUI(View rootView, List<BaseExModel> list) {
        TextView textView1 = rootView.findViewById(R.id.textView1);
        TextView textView2 = rootView.findViewById(R.id.textView2);
        Button finalWorkBtn = rootView.findViewById(R.id.finalWorkBtn);

        if (list == null || list.isEmpty()) {
            textView1.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
            finalWorkBtn.setVisibility(View.GONE);
            workoutRecyclerView.setVisibility(View.GONE);
        } else {
            textView1.setVisibility(View.GONE);
            textView2.setVisibility(View.GONE);
            finalWorkBtn.setVisibility(View.VISIBLE);
            workoutRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}