package com.example.workoutapp;

import java.util.List;

public class PresetModel {
    private final String presetName;
    private final List<ExModel> exercises;

    public PresetModel(String presetName, List<ExModel> exercises) {
        this.presetName = presetName;
        this.exercises = exercises;
    }

    public String getPresetName() {
        return presetName;
    }

    public List<ExModel> getExercises() {
        return exercises;
    }
}
