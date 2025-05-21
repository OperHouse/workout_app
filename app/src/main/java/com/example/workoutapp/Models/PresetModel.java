package com.example.workoutapp.Models;

import java.io.Serializable;
import java.util.List;

public class PresetModel implements Serializable {
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
