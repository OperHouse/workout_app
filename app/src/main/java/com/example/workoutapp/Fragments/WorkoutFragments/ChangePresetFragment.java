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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.WorkoutAdapters.ExAdapter;
import com.example.workoutapp.Data.WorkoutDao.ExerciseDao;
import com.example.workoutapp.Data.WorkoutDao.PresetDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.WorkoutModels.BaseExModel;
import com.example.workoutapp.Models.WorkoutModels.ExerciseModel;
import com.example.workoutapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChangePresetFragment extends Fragment {

    private ExerciseModel exerciseModel;
    private List<BaseExModel> exList;
    private ExerciseDao ExDao;
    private PresetDao PresetDao;
    private ExAdapter exAdapter;
    private RecyclerView exRecycler;
    private SearchView searchView;
    private List<BaseExModel> selectedExercises = new ArrayList<>();  // Список выделенных элементов

    public ChangePresetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ExDao = new ExerciseDao(MainActivity.getAppDataBase());
        this.PresetDao = new PresetDao(MainActivity.getAppDataBase());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootViewChangePresetFragment = inflater.inflate(R.layout.fragment_change_preset, container, false);
        if (getArguments() != null) {
            exerciseModel = (ExerciseModel) getArguments().getSerializable("preset");
        }

        RecyclerView exRecycler = RootViewChangePresetFragment.findViewById(R.id.ExerciseRecyclerViewPresets);
        ImageButton backBtn = RootViewChangePresetFragment.findViewById(R.id.imageButtonBack);
        Button nextBtn = RootViewChangePresetFragment.findViewById(R.id.nextBtn);
        TextView text = RootViewChangePresetFragment.findViewById(R.id.textView4);
        searchView = RootViewChangePresetFragment.findViewById(R.id.searchExercise4);

        text.setText(exerciseModel.getPresetName());

        exList = ExDao.getAllExercises();

        // Установка состояния выделения для упражнений в списке
        for (BaseExModel a : exerciseModel.getExercises()) {
            for (BaseExModel b : exList) {
                if (Objects.equals(a.getExName(), b.getExName()) &&
                        Objects.equals(a.getExType(), b.getExType()) &&
                        Objects.equals(a.getBodyType(), b.getBodyType())) {
                    b.setIsPressed(true);
                    break;
                }
            }
        }

        // Инициализация адаптера
        exAdapter = new ExAdapter(ChangePresetFragment.this, true);
        exAdapter.updateExList(exList); // Заполняем адаптер списком упражнений
        exRecycler.setHasFixedSize(true);
        exRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        exRecycler.setAdapter(exAdapter);

        backBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = getFragmentManager();
            assert fragmentManager != null;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            try {
                fragmentTransaction.replace(R.id.frameLayout, Selection_Ex_Preset_Fragment.class.newInstance());
            } catch (IllegalAccessException | java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
            fragmentTransaction.commit();
        });

        nextBtn.setOnClickListener(v -> showDialogConfirmation(exAdapter));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus(); // Убираем клавиатуру
                exRecycler.requestFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                exAdapter.updateExListFiltered(newText);
                return true;
            }
        });

        return RootViewChangePresetFragment;
    }

    private void showDialogConfirmation(ExAdapter exAdapter){
        Dialog dialogCreatePreset = new Dialog(requireContext());
        dialogCreatePreset.setContentView(R.layout.confirm_dialog_preset);
        Objects.requireNonNull(dialogCreatePreset.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreatePreset.show();
        searchView.clearFocus();

        EditText namePreset = dialogCreatePreset.findViewById(R.id.editText);
        Button btnAdd = dialogCreatePreset.findViewById(R.id.btnAdd);
        Button btnChanel = dialogCreatePreset.findViewById(R.id.btnChanel);
        TextView text1 = dialogCreatePreset.findViewById(R.id.textView);
        TextView text2 = dialogCreatePreset.findViewById(R.id.text1);

        text1.setText(exerciseModel.getPresetName());
        text2.setText("Введите новое имя пресета");
        namePreset.setText(exerciseModel.getPresetName());
        btnAdd.setText("Изменить");

        if(dialogCreatePreset.getWindow() != null){
            dialogCreatePreset.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        btnChanel.setOnClickListener(v -> {
            dialogCreatePreset.dismiss();
            searchView.clearFocus();
        });

        btnAdd.setOnClickListener(v -> {
            String presetName = namePreset.getText().toString().trim();
            if (presetName.isEmpty()) {
                namePreset.setError("Пожалуйста, введите название упражнения");
                return;
            }

            ExerciseModel newPreset = new ExerciseModel(presetName,exAdapter.getList());
            if (!exerciseModel.equals(newPreset)){
                PresetDao.updatePreset(exerciseModel.getPresetName(),newPreset);
                Toast.makeText(requireContext(), "Пресет изменен!", Toast.LENGTH_SHORT).show();
                dialogCreatePreset.dismiss();
            } else {
                dialogCreatePreset.dismiss();
                Toast.makeText(requireContext(), "Окно закрыто", Toast.LENGTH_SHORT).show();
            }

            FragmentManager fragmentManager = getFragmentManager();
            assert fragmentManager != null;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            try {
                fragmentTransaction.replace(R.id.frameLayout, Selection_Ex_Preset_Fragment.class.newInstance());
            } catch (IllegalAccessException | java.lang.InstantiationException e) {
                throw new RuntimeException(e);
            }
            fragmentTransaction.commit();
        });
    }

    private void filterExerciseList(String query, ExAdapter adapter) {
        List<BaseExModel> filteredList = new ArrayList<>();
        for (BaseExModel ex : exList) {
            if (ex.getExName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(ex);
            }
        }
        //adapter.updateExListFiltered(query, filteredList);

    }

    public void clearSearchFocus() {
        if (searchView != null) {
            searchView.clearFocus();
        }
    }
}