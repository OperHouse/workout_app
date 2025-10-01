package com.example.workoutapp.Models.WorkoutModels;

import java.util.ArrayList;
import java.util.List;

public class ExerciseModel {
    private long exercise_id;
    private String exerciseName;
    private String exerciseType;
    private String exerciseBodyType;
    private String ex_Data = "";
    private String state = "unfinished";

    // Единый список для сетов, который может содержать разные типы объектов
    private final List<Object> sets;

    // Конструктор для пресета
    public ExerciseModel(long id, String presetName, List<Object> sets) {
        this.exercise_id = id;
        this.exerciseName = presetName;
        this.exerciseType = null;
        this.exerciseBodyType = null;
        this.ex_Data = null;
        this.state = null;

        // Вызов метода для глубокого копирования
        this.sets = deepCopySets(sets);
    }

    // Конструктор для тренировки (копирование)
    public ExerciseModel(ExerciseModel other) {
        this.exercise_id = other.exercise_id;
        this.exerciseName = other.exerciseName;
        this.exerciseType = other.exerciseType;
        this.exerciseBodyType = other.exerciseBodyType;
        this.ex_Data = other.ex_Data;
        this.state = other.state;

        // Вызов метода для глубокого копирования
        this.sets = deepCopySets(other.getSets());
    }

    // Основной конструктор, который вы будете использовать в DAO
    public ExerciseModel(long id, String name, String type, String bodyType, String date, String currentState, List<Object> sets) {
        this.exercise_id = id;
        this.exerciseName = name;
        this.exerciseType = type;
        this.exerciseBodyType = bodyType;
        this.ex_Data = date;
        this.state = currentState;

        // Вызов метода для глубокого копирования
        this.sets = deepCopySets(sets);
    }

    // Приватный метод для выполнения глубокого копирования
    private List<Object> deepCopySets(List<Object> sourceSets) {
        List<Object> newSets = new ArrayList<>();
        if (sourceSets != null) {
            for (Object set : sourceSets) {
                if (set instanceof StrengthSetModel) {
                    // Используем конструктор копирования для StrengthSetModel
                    newSets.add(new StrengthSetModel((StrengthSetModel) set));
                } else if (set instanceof CardioSetModel) {
                    // Используем конструктор копирования для CardioSetModel
                    newSets.add(new CardioSetModel((CardioSetModel) set));
                } else if (set instanceof BaseExModel) {
                    // Используем конструктор копирования для BaseExModel
                    newSets.add(new BaseExModel((BaseExModel) set));
                }
                // Если есть другие типы моделей, их нужно добавить сюда
            }
        }
        return newSets;
    }


    public long getExercise_id() {
        return exercise_id;
    }

    public void setExercise_id(long exercise_id) {
        this.exercise_id = exercise_id;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public String getExerciseBodyType() {
        return exerciseBodyType;
    }

    public void setExerciseBodyType(String exerciseBodyType) {
        this.exerciseBodyType = exerciseBodyType;
    }

    public String getEx_Data() {
        return ex_Data;
    }

    public void setEx_Data(String ex_Data) {
        this.ex_Data = ex_Data;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    // Новый геттер для списка сетов
    public List<Object> getSets() {
        return sets;
    }
    /**
     * Добавляет новый сет в список сетов для этого упражнения.
     * @param set Объект сета для добавления.
     */
    public void addSet(Object set) {
        this.sets.add(set);
    }

}