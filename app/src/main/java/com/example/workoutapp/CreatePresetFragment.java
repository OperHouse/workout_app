package com.example.workoutapp;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;


public class CreatePresetFragment extends Fragment {
    DataBase dataBase;
    private List<ExModel> exList;  // основной список всех элементов
    private List<ExModel> clickedList;  // список нажатых элементов

    public CreatePresetFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBase = new DataBase(requireContext());
        List<ExModel> exList = dataBase.getAllExercise();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View RootViewCreatePresetFragment = inflater.inflate(R.layout.fragment_create_preset, container, false);

        RecyclerView exRecycler = RootViewCreatePresetFragment.findViewById(R.id.ExerciseRecyclerViewPresets);
        ImageButton backBtn = RootViewCreatePresetFragment.findViewById(R.id.imageButtonBack);
        Button nextBtn = RootViewCreatePresetFragment.findViewById(R.id.nextBtn);


        exList = dataBase.getAllExercise();
        ExAdapter exAdapter = new ExAdapter( CreatePresetFragment.this, true, exList);
        exAdapter.updateExList(exList);
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
                showDialogConfirmation(exAdapter);
            }
        });



        return RootViewCreatePresetFragment;
    }


    private void showDialogConfirmation(ExAdapter exAdapter){
        Dialog dialogCreatePreset = new Dialog(requireContext());
        dialogCreatePreset.setContentView(R.layout.confirm_dialog_preset);
        Objects.requireNonNull(dialogCreatePreset.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreatePreset.show();

        EditText namePreset = dialogCreatePreset.findViewById(R.id.editText);
        Button btnAdd = dialogCreatePreset.findViewById(R.id.btnAdd);
        Button btnChanel = dialogCreatePreset.findViewById(R.id.btnChanel);


        if(dialogCreatePreset.getWindow() != null){
            dialogCreatePreset.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        btnChanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {dialogCreatePreset.dismiss();}
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String presetName = namePreset.getText().toString().trim();
                if (presetName.isEmpty()) {
                    namePreset.setError("Пожалуйста, введите название упражнения");
                    return;
                }
                PresetModel newPreset = new PresetModel(presetName,exAdapter.getList());
                dataBase.addPreset(newPreset);
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
}