package com.example.workoutapp.Models.WorkoutModels;

import java.io.Serializable;
import java.util.List;

public class ExerciseModel implements Serializable {
    private final String presetName;
    private final List<BaseExModel> exercises;

    public ExerciseModel(String presetName, List<BaseExModel> exercises) {
        this.presetName = presetName;
        this.exercises = exercises;
    }

    public String getPresetName() {
        return presetName;
    }

    public List<BaseExModel> getExercises() {
        return exercises;
    }
}
