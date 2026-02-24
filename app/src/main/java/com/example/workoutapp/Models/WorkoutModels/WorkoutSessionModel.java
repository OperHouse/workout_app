package com.example.workoutapp.Models.WorkoutModels;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class WorkoutSessionModel {
    @PropertyName("date")
    private String workoutDate; // "2026-02-17"
    private String workoutTitle;
    private List<ExerciseModel> exercises;

    public WorkoutSessionModel() {
    }

    public WorkoutSessionModel(String workoutDate, List<ExerciseModel> exercises) {
        this.workoutDate = workoutDate;
        this.exercises = exercises;
        this.workoutTitle = calculateTitle(exercises); // Вычисляем заголовок при создании
    }

    private String calculateTitle(List<ExerciseModel> exercises) {
        if (exercises == null || exercises.isEmpty()) {
            return "Пустая тренировка";
        }

        int cardioCount = 0;
        int strengthCount = 0;

        for (ExerciseModel ex : exercises) {
            // Проверяем тип упражнения (используем equalsIgnoreCase для надежности)
            if ("Кардио".equalsIgnoreCase(ex.getExerciseType()) || "Время".equalsIgnoreCase(ex.getExerciseType())) {
                cardioCount++;
            } else {
                // Все остальные (Силовые, Вес и т.д.) считаем за силовые
                strengthCount++;
            }
        }

        if (cardioCount > strengthCount) {
            return "Кардио тренировка";
        } else if (strengthCount > cardioCount) {
            return "Силовая тренировка";
        } else {
            return "Смешанная тренировка"; // Если поровну
        }
    }

    // Геттеры
    @PropertyName("date")
    public String getWorkoutDate() { return workoutDate; }
    @PropertyName("date")
    public void setWorkoutDate(String workoutDate) { this.workoutDate = workoutDate; }

    public String getWorkoutTitle() { return workoutTitle; }
    public List<ExerciseModel> getExercises() { return exercises; }

    // Сеттер для упражнений, если список изменится после создания объекта
    public void setExercises(List<ExerciseModel> exercises) {
        this.exercises = exercises;
        this.workoutTitle = calculateTitle(exercises);
    }
}