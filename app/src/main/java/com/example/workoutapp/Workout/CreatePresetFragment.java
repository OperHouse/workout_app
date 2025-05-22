package com.example.workoutapp.Workout;

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

import com.example.workoutapp.Adapters.ExAdapter;
import com.example.workoutapp.DAO.ExerciseDao;
import com.example.workoutapp.DAO.PresetDao;
import com.example.workoutapp.MainActivity;
import com.example.workoutapp.Models.ExModel;
import com.example.workoutapp.Models.PresetModel;
import com.example.workoutapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CreatePresetFragment extends Fragment {
    ExerciseDao ExDao;
    PresetDao PresetDao;

    private List<ExModel> exList;
    SearchView searchView;

    public CreatePresetFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ExDao = new ExerciseDao(MainActivity.getAppDataBase());
        this.PresetDao = new PresetDao(MainActivity.getAppDataBase());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View RootViewCreatePresetFragment = inflater.inflate(R.layout.fragment_create_preset, container, false);

        RecyclerView exRecycler = RootViewCreatePresetFragment.findViewById(R.id.ExerciseRecyclerViewPresets);
        ImageButton backBtn = RootViewCreatePresetFragment.findViewById(R.id.imageButtonBack);
        Button nextBtn = RootViewCreatePresetFragment.findViewById(R.id.nextBtn);
        searchView = RootViewCreatePresetFragment.findViewById(R.id.searchExercise3);


        exList = ExDao.getAllExercises();
        ExAdapter exAdapter = new ExAdapter( CreatePresetFragment.this, true);
        exAdapter.updateExList2(exList);
        exRecycler.setHasFixedSize(true);
        exRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        exRecycler.setAdapter(exAdapter);



        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                assert fragmentManager != null;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                try {
                    fragmentTransaction.replace(R.id.frameLayout, AddExFragment.class.newInstance());
                } catch (IllegalAccessException | java.lang.InstantiationException e) {
                    throw new RuntimeException(e);
                }
                fragmentTransaction.commit();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exAdapter.getList().isEmpty()) {
                    Toast.makeText(requireContext(), "Выберите хотя бы одно упражнение", Toast.LENGTH_SHORT).show();
                    return;
                }
                searchView.clearFocus();
                exRecycler.requestFocus();
                showDialogConfirmation(exAdapter);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Убираем фокус после отправки
                searchView.clearFocus();
                exRecycler.requestFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                exAdapter.updateExListFiltered(newText);
                return true;
            }
        });



        return RootViewCreatePresetFragment;
    }

    private void filterExerciseList(String query, ExAdapter adapter) {
        List<ExModel> filteredList = new ArrayList<>();
        for (ExModel ex : exList) {
            if (ex.getExName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(ex);
            }
        }
        //adapter.updateExListFiltered(query, filteredList);

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


        if(dialogCreatePreset.getWindow() != null){
            dialogCreatePreset.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        btnChanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCreatePreset.dismiss();
                searchView.clearFocus();
                
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String presetName = namePreset.getText().toString().trim();

                if (presetName.isEmpty()) {
                    namePreset.setError("Пожалуйста, введите название пресета");
                    return;
                }

                if (exAdapter.getList().isEmpty()) {
                    Toast.makeText(requireContext(), "Выберите хотя бы одно упражнение", Toast.LENGTH_SHORT).show();
                    return;
                }

                PresetModel newPreset = new PresetModel(presetName, exAdapter.getList());
                PresetDao.addPreset(newPreset);
                dialogCreatePreset.dismiss();

                Toast.makeText(requireContext(), "Пресет создан!", Toast.LENGTH_SHORT).show();

                FragmentManager fragmentManager = getFragmentManager();
                assert fragmentManager != null;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                try {
                    fragmentTransaction.replace(R.id.frameLayout, AddExFragment.class.newInstance());
                } catch (IllegalAccessException | java.lang.InstantiationException e) {
                    throw new RuntimeException(e);
                }
                fragmentTransaction.commit();
            }
        });
    }
    public void clearSearchFocus() {
        if (searchView != null) {
            searchView.clearFocus();
        }
    }

}