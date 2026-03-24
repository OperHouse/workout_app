package com.example.workoutapp.Tools.DeletionHandlers;

public enum DeletionTaskType {
    WORKOUT_EX_DELETE("workout_ex_delete"),
    BASE_EX_DELETE("base_ex_delete"),
    WORKOUT_PRESET_DELETE("workout_preset_delete"),
    MEAL_PRESET("meal_preset"),
    FOOD("food"),
    MEAL_DIARY("meal_diary");

    private final String key;
    DeletionTaskType(String key) { this.key = key; }
    public String getKey() { return key; }

    public static DeletionTaskType fromString(String text) {
        for (DeletionTaskType type : DeletionTaskType.values()) {
            if (type.key.equalsIgnoreCase(text)) return type;
        }
        return MEAL_DIARY;
    }
}
