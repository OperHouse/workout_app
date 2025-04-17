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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workoutapp.Adapters.ExAdapter;
import com.example.workoutapp.Data.DataBase;
import com.example.workoutapp.Models.ExModel;
import com.example.workoutapp.Models.PresetModel;
import com.example.workoutapp.R;

import java.util.List;
import java.util.Objects;

public class ChangePresetFragment extends Fragment {

    private PresetModel presetModel;
    private List<ExModel> exList;
    private DataBase dataBase;
    private ExAdapter exAdapter;
    private RecyclerView exRecycler;
    public ChangePresetFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBase = new DataBase(requireContext());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootViewChangePresetFragment = inflater.inflate(R.layout.fragment_change_preset, container, false);
        if (getArguments() != null) {
            presetModel = (PresetModel) getArguments().getSerializable("preset");
        }


        RecyclerView exRecycler = RootViewChangePresetFragment.findViewById(R.id.ExerciseRecyclerViewPresets);
        ImageButton backBtn = RootViewChangePresetFragment.findViewById(R.id.imageButtonBack);
        Button nextBtn = RootViewChangePresetFragment.findViewById(R.id.nextBtn);
        TextView text = RootViewChangePresetFragment.findViewById(R.id.textView4);

        text.setText(presetModel.getPresetName());

        exList = dataBase.getAllExercise();

        for (ExModel a: presetModel.getExercises()) {
            for (ExModel b: exList) {
                if (Objects.equals(a.getExName(), b.getExName()) &&
                        Objects.equals(a.getExType(), b.getExType()) &&
                        Objects.equals(a.getBodyType(), b.getBodyType())) {
                    b.setIsPressed(true);
                    break; // Выход из внутреннего цикла, так как совпадение найдено
                }
            }
        }

        ExAdapter exAdapter = new ExAdapter( ChangePresetFragment.this, true);
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

        return RootViewChangePresetFragment;
    }

    private void showDialogConfirmation(ExAdapter exAdapter){
        Dialog dialogCreatePreset = new Dialog(requireContext());
        dialogCreatePreset.setContentView(R.layout.confirm_dialog_preset);
        Objects.requireNonNull(dialogCreatePreset.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogCreatePreset.show();

        EditText namePreset = dialogCreatePreset.findViewById(R.id.editText);
        Button btnAdd = dialogCreatePreset.findViewById(R.id.btnAdd);
        Button btnChanel = dialogCreatePreset.findViewById(R.id.btnChanel);
        TextView text1 = dialogCreatePreset.findViewById(R.id.textView);
        TextView text2 = dialogCreatePreset.findViewById(R.id.text1);

        text1.setText(presetModel.getPresetName());
        text2.setText("Введите новое имя пресета");
        namePreset.setText(presetModel.getPresetName());
        btnAdd.setText("Изменить");


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
                if (!presetModel.equals(newPreset)){
                    dataBase.changePreset(presetModel.getPresetName(),newPreset);
                    Toast.makeText(requireContext(), "Пресет создан!", Toast.LENGTH_SHORT).show();
                    dialogCreatePreset.dismiss();

                }else {dialogCreatePreset.dismiss();
                    Toast.makeText(requireContext(), "Окно закрыто", Toast.LENGTH_SHORT).show();}





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