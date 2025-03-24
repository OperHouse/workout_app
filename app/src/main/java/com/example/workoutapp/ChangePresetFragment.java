package com.example.workoutapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ChangePresetFragment extends Fragment {

    private PresetModel presetModel;
    public ChangePresetFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        // Получаем объект PresetModel из Bundle
        if (getArguments() != null) {
            presetModel = (PresetModel) getArguments().getSerializable("preset");
        }

        // Проверим полученные данные
        if (presetModel != null) {
            Log.d("ChangePresetFragment", "Preset Name: " + presetModel.getPresetName());
            for (ExModel ex : presetModel.getExercises()) {
                Log.d("ChangePresetFragment", "Exercise Name: " + ex.getExName());
            }
        }
        return inflater.inflate(R.layout.fragment_change_preset, container, false);
    }
}