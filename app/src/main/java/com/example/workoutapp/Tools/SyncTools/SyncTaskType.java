package com.example.workoutapp.Tools.SyncTools;

public enum SyncTaskType {
    MEAL("meal"),
    MEAL_PRESET("meal_preset"),
    WORKOUT_EX("workout_ex"),
    BASE_EXERCISE("base_exercise"),
    WORKOUT_PRESET("workout_preset"),
    USER_PROFILE("user_profile"),
    WEIGHT_HISTORY("weight_history"),
    ACTIVITY_GOAL("activity_goal"),
    GENERAL_GOAL("general_goal"),
    FOOD_GOAL("food_goal"),
    DAILY_FOOD("daily_food"),
    DAILY_ACTIVITY("daily_activity");

    private final String typeString;

    SyncTaskType(String typeString) {
        this.typeString = typeString;
    }

    public String getTypeString() {
        return typeString;
    }

    // Метод для безопасного получения Enum из строки
    public static SyncTaskType fromString(String text) {
        for (SyncTaskType b : SyncTaskType.values()) {
            if (b.typeString.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
