package com.example.workoutapp.Fragments.WorkoutFragments;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.WorkoutAdapters.ExAdapter;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.CONNECTING_WORKOUT_PRESET_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.WorkoutMode;

import java.util.List;
import java.util.Objects;


public class CreatePresetFragment extends Fragment {
    // Используем новые DAO-классы
    WORKOUT_PRESET_NAME_TABLE_DAO presetNameDao;
    CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingPresetDao;
    BASE_EXERCISE_TABLE_DAO baseExerciseDao;

    private List<BaseExModel> exList;
    private ExAdapter exAdapter;
    SearchView searchView;

    public CreatePresetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Инициализируем новые DAO-классы
        presetNameDao = new WORKOUT_PRESET_NAME_TABLE_DAO(MainActivity.getAppDataBase());
        connectingPresetDao = new CONNECTING_WORKOUT_PRESET_TABLE_DAO(MainActivity.getAppDataBase());
        baseExerciseDao = new BASE_EXERCISE_TABLE_DAO(MainActivity.getAppDataBase());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_preset, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.ExercisePresets_RV);
        Button createPresetBtn = view.findViewById(R.id.createPresetBtn);
        searchView = view.findViewById(R.id.exercise_SV);

        // Получаем полный список базовых упражнений из DAO
        exList = baseExerciseDao.getAllExercises();
        exAdapter = new ExAdapter(requireContext(), exList, WorkoutMode.SELECTED);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(exAdapter);

        createPresetBtn.setOnClickListener(v -> createPresetDialog());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //exAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return view;
    }

    private void createPresetDialog() {
        Dialog dialogCreatePreset = new Dialog(requireContext());
        dialogCreatePreset.setContentView(R.layout.confirm_dialog_preset);
        Objects.requireNonNull(dialogCreatePreset.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreatePreset.setCancelable(false);
        dialogCreatePreset.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        EditText namePreset = dialogCreatePreset.findViewById(R.id.namePreset_ET);
        Button createBtn = dialogCreatePreset.findViewById(R.id.createBtn);
        ImageButton closeBtn = dialogCreatePreset.findViewById(R.id.closeBtn);

        closeBtn.setOnClickListener(v -> dialogCreatePreset.dismiss());

        createBtn.setOnClickListener(v -> {
            String presetName = namePreset.getText().toString().trim();
            List<BaseExModel> selectedExercises = exAdapter.getSelectedItems();

            if (presetName.isEmpty()) {
                namePreset.setError("Пожалуйста, введите название пресета");
                return;
            }

            if (selectedExercises.isEmpty()) {
                Toast.makeText(requireContext(), "Выберите хотя бы одно упражнение", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Сохраняем имя пресета и получаем ID
            long newPresetId = presetNameDao.addPresetName(presetName);

            // 2. Сохраняем выбранные упражнения в соединяющую таблицу
            for (BaseExModel exercise : selectedExercises) {
                connectingPresetDao.addPresetExercise(newPresetId, exercise.getBase_ex_id());
            }

            dialogCreatePreset.dismiss();

            Toast.makeText(requireContext(), "Пресет создан!", Toast.LENGTH_SHORT).show();

            FragmentManager fragmentManager = getFragmentManager();
            assert fragmentManager != null;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, new Selection_Ex_Preset_Fragment());
            fragmentTransaction.commit();
        });
        dialogCreatePreset.show();
    }

    public void clearSearchFocus() {
        if (searchView != null) {
            searchView.clearFocus();
        }
    }
}