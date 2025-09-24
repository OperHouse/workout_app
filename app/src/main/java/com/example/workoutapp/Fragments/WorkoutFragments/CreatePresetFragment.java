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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.WorkoutAdapters.ExAdapter;
import com.example.workoutapp.Data.WorkoutDao.BASE_EXERCISE_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.CONNECTING_WORKOUT_PRESET_TABLE_DAO;
import com.example.workoutapp.Data.WorkoutDao.WORKOUT_PRESET_NAME_TABLE_DAO;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.R;
import com.example.workoutapp.Tools.WorkoutMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CreatePresetFragment extends Fragment {

    private WORKOUT_PRESET_NAME_TABLE_DAO presetNameDao;
    private CONNECTING_WORKOUT_PRESET_TABLE_DAO connectingPresetDao;
    private BASE_EXERCISE_TABLE_DAO baseExerciseDao;

    private List<BaseExModel> exList;
    private ExerciseModel preset;
    private ExAdapter exAdapter;
    private WorkoutMode currentState = WorkoutMode.CREATE_PRESET;
    private SearchView searchView;

    public CreatePresetFragment() {
    }

    public CreatePresetFragment(ExerciseModel preset, WorkoutMode mode) {
        this.preset = new ExerciseModel(preset); // копия
        this.currentState = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        ImageButton back_BTN = view.findViewById(R.id.imageButtonBack);
        searchView = view.findViewById(R.id.exercise_SV);

        exList = baseExerciseDao.getAllExercises();

        if (currentState == WorkoutMode.EDIT_PRESET && preset != null) {
            List<Long> presetExerciseIds = connectingPresetDao.getBaseExIdsByPresetId(preset.getExercise_id());
            List<BaseExModel> updatedList = new ArrayList<>();

            // 1. Добавляем упражнения из пресета (с выделением)
            for (Long exId : presetExerciseIds) {
                BaseExModel exerciseFromDb = baseExerciseDao.getExerciseById(exId);
                if (exerciseFromDb != null) {
                    BaseExModel copy = new BaseExModel(exerciseFromDb); // глубокое копирование
                    copy.setIsPressed(true); // выделяем
                    updatedList.add(copy);
                }
            }

            // 2. Добавляем остальные упражнения, которых нет в пресете
            for (BaseExModel ex : exList) {
                if (!presetExerciseIds.contains(ex.getBase_ex_id())) {
                    updatedList.add(new BaseExModel(ex)); // глубокое копирование
                }
            }

            exList = new ArrayList<>(updatedList);
        }

        exAdapter = new ExAdapter(this, requireContext(), WorkoutMode.SELECTED);
        exAdapter.updateExList(exList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(exAdapter);

        // текст кнопки в зависимости от режима
        if (currentState == WorkoutMode.EDIT_PRESET) {
            createPresetBtn.setText("Сохранить изменения");
        }

        createPresetBtn.setOnClickListener(v -> showPresetDialog());

        back_BTN.setOnClickListener(v -> {
            FragmentManager fragmentManager = getFragmentManager();
            assert fragmentManager != null;
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }
        });

        return view;
    }

    private void showPresetDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.confirm_dialog_preset);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        EditText namePreset = dialog.findViewById(R.id.namePreset_ET);
        Button createBtn = dialog.findViewById(R.id.createBtn);
        Button closeBtn = dialog.findViewById(R.id.closeBtn);

        // если редактирование — показываем имя
        if (currentState == WorkoutMode.EDIT_PRESET && preset != null) {
            namePreset.setText(preset.getExerciseName());
            createBtn.setText("Сохранить");
        }

        closeBtn.setOnClickListener(v -> dialog.dismiss());

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

            if (currentState == WorkoutMode.CREATE_PRESET) {
                long newPresetId = presetNameDao.addPresetName(presetName);
                for (BaseExModel exercise : selectedExercises) {
                    connectingPresetDao.addPresetExercise(newPresetId, exercise.getBase_ex_id());
                }
                Toast.makeText(requireContext(), "Пресет создан!", Toast.LENGTH_SHORT).show();

            } else if (currentState == WorkoutMode.EDIT_PRESET && preset != null) {
                // обновляем имя
                if (!preset.getExerciseName().equals(presetName)) {
                    presetNameDao.updatePresetName(preset.getExercise_id(), presetName);
                }
                // очищаем связи
                connectingPresetDao.deleteExercisesByPresetId(preset.getExercise_id());
                // добавляем новые связи
                for (BaseExModel exercise : selectedExercises) {
                    connectingPresetDao.addPresetExercise(preset.getExercise_id(), exercise.getBase_ex_id());
                }
                Toast.makeText(requireContext(), "Пресет обновлён!", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();

            FragmentManager fragmentManager = getFragmentManager();
            assert fragmentManager != null;
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }
        });

        dialog.show();
    }

    public void clearSearchFocus() {
        if (searchView != null) {
            searchView.clearFocus();
        }
    }
}